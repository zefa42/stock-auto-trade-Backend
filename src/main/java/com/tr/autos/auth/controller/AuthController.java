package com.tr.autos.auth.controller;

import com.tr.autos.auth.dto.request.SignupRequestDto;
import com.tr.autos.auth.dto.request.LoginRequestDto;
import com.tr.autos.auth.dto.response.LoginResponseDto;
import com.tr.autos.auth.dto.response.SignupResponseDto;
import com.tr.autos.auth.service.AuthService;
import com.tr.autos.domain.user.User;
import com.tr.autos.domain.user.repository.UserRepository;
import com.tr.autos.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody @Valid SignupRequestDto signupRequestDto) {
        SignupResponseDto res = authService.signup(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        LoginResponseDto res = authService.login(loginRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    // logout 교체  (Access/Refresh 모두 헤더로 받음)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value="Authorization", required=false) String accessBearer,
            @RequestHeader(value="X-Refresh-Token", required=false) String refreshBearer) {

        if (accessBearer == null || !accessBearer.startsWith("Bearer ")
                || refreshBearer == null || !refreshBearer.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String accessToken = accessBearer.substring(7);
        String refreshToken = refreshBearer.substring(7);

        try {
            // Access 파싱
            var aClaims = jwtTokenProvider.parse(accessToken).getBody();
            if (!"access".equals(aClaims.get("typ"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String accessJti = String.valueOf(aClaims.get("jti"));
            long accessTtlSec = (aClaims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;

            // Refresh 파싱
            var rClaims = jwtTokenProvider.parse(refreshToken).getBody();
            if (!"refresh".equals(rClaims.get("typ"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String email = rClaims.getSubject();
            var user = userRepository.findByEmail(email).orElseThrow();

            // RT 무효화
            String rtKey = "rt:" + user.getId();
            stringRedisTemplate.delete(rtKey);

            // ★ Access 블랙리스트 등록 (남은 만료 시간만큼 TTL)
            if (accessTtlSec > 0) {
                String blKey = "bl:a:" + accessJti;
                stringRedisTemplate.opsForValue()
                        .set(blKey, "1", Duration.ofSeconds(accessTtlSec));
            }

            return ResponseEntity.noContent().build();
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@RequestHeader(value="Authorization", required=false) String bearer) {
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        var refreshToken = bearer.substring(7);
        try {
            var claims = jwtTokenProvider.parse(refreshToken).getBody();

            // 1) 리프레시 토큰인지 확인
            if (!"refresh".equals(claims.get("typ"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 2) jti 추출
            String tokenJti = String.valueOf(claims.get("jti"));
            String email = claims.getSubject();

            // 3) 사용자 조회
            var user = userRepository.findByEmail(email).orElseThrow();

            // 4) Redis의 jti와 일치하는지 확인
            String redisKey = "rt:" + user.getId();
            String storedJti = stringRedisTemplate.opsForValue().get(redisKey);
            if (storedJti == null || !storedJti.equals(tokenJti)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // ★ 회전: 새 RT jti 저장
            String newRefreshJti = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue()
                    .set(redisKey, newRefreshJti, Duration.ofSeconds(jwtTokenProvider.refreshTtlSeconds()));

            // ★ 새 Access도 jti 부여
            String newAccessJti = UUID.randomUUID().toString();
            String newAccess = jwtTokenProvider.createAccessToken(user.getEmail(), user.getName(), newAccessJti, user.getRole());
            String newRefresh = jwtTokenProvider.createRefreshToken(email, newRefreshJti);

            return ResponseEntity.ok(new LoginResponseDto(email, user.getName(), user.getRole().name(), newAccess, newRefresh));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(java.security.Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        // 여기서 직접 401 만들지 말고, principal이 있다고 가정 (인증 필수 엔드포인트이므로)
        return ResponseEntity.ok(Map.of(
                 "email", user.getEmail()
                ,"name", user.getName()
                ,"role", user.getRole().name()
        ));
    }
}
