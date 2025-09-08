package com.tr.autos.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private String email;
    private String name;
    private String role;
    private String accessToken;
    private String refreshToken;
}
