package com.tr.autos.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponseDto {
    private String email;
    private String name;
}
