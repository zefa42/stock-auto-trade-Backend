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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value="Authorization", required=false) String bearer) {
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        var refreshToken = bearer.substring(7);
        try {
            var claims = jwtTokenProvider.parse(refreshToken).getBody();
            if (!"refresh".equals(claims.get("typ"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String email = claims.getSubject();
            var user = userRepository.findByEmail(email).orElseThrow();

            String redisKey = "rt:" + user.getId();
            stringRedisTemplate.delete(redisKey);

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

            // 5) 회전(rotate): 새 jti 발급 & Redis 갱신
            String newJti = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue()
                    .set(redisKey, newJti, Duration.ofSeconds(jwtTokenProvider.refreshTtlSeconds()));

            // 6) 새 토큰들 발급
            String newAccess = jwtTokenProvider.createAccessToken(user.getEmail(), user.getName());
            String newRefresh = jwtTokenProvider.createRefreshToken(email, newJti);

            return ResponseEntity.ok(new LoginResponseDto(email, user.getName(), newAccess, newRefresh));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(Map.of("email", principal.getName()));
    }
}
