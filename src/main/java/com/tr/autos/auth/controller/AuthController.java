package com.tr.autos.auth.controller;

import com.tr.autos.auth.dto.request.SignupRequestDto;
import com.tr.autos.auth.dto.response.SignupResponseDto;
import com.tr.autos.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody @Valid SignupRequestDto signupRequestDto) {
        SignupResponseDto res = authService.signup(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
