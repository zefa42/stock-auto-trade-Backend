package com.tr.autos.auth.controller;

import com.tr.autos.auth.dto.request.SignupRequestDto;
import com.tr.autos.auth.dto.request.LoginRequestDto;
import com.tr.autos.auth.dto.response.LoginResponseDto;
import com.tr.autos.auth.dto.response.SignupResponseDto;
import com.tr.autos.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        LoginResponseDto res = authService.login(loginRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}
