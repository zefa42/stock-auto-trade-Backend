package com.tr.autos.auth.service;

import com.tr.autos.auth.dto.request.SignupRequestDto;
import com.tr.autos.auth.dto.response.SignupResponseDto;
import com.tr.autos.domain.user.User;
import com.tr.autos.domain.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        // 이메일 중복 확인
        if(userRepository.existsByEmail(signupRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 Email 입니다.");
        }

        // 비밀번호 해시 암호화
        String hashedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        // 회원정보 저장
        User user = User.builder()
                .email(signupRequestDto.getEmail())
                .passwordHash(hashedPassword)
                .name(signupRequestDto.getName())
                .build();
        userRepository.save(user);

        // 반환
        return new SignupResponseDto(user.getEmail(), user.getName());
    }
}
