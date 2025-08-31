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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

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

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@RequestHeader(value="Authorization", required=false) String bearer) {
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
            String newAccess = jwtTokenProvider.createAccessToken(user.getEmail(), user.getName());
            String newRefresh = jwtTokenProvider.createRefreshToken(email); // (선택) 롤링
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
