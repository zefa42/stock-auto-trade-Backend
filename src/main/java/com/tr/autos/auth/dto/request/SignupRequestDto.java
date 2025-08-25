package com.tr.autos.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, message = "비밀번호는 최소 6자리 이상입니다.")
    private String password;
    //private String confirmPassword;  <-  비밀번호 확인은 프론트에서 검증

    @NotBlank
    @Size(max = 50)
    private String name;
}
