package com.tr.autos.auth.service;

import com.tr.autos.auth.dto.request.LoginRequestDto;
import com.tr.autos.auth.dto.request.SignupRequestDto;
import com.tr.autos.auth.dto.response.LoginResponseDto;
import com.tr.autos.auth.dto.response.SignupResponseDto;
import com.tr.autos.domain.user.User;
import com.tr.autos.domain.user.repository.UserRepository;
import com.tr.autos.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입 로직
    @Transactional
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        // 이메일 중복 확인
        if(userRepository.existsByEmail(signupRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 Email 입니다.");
        }

        // 비밀번호 해시 암호화
        String hashedPassword = passwordEncoder.encode(signupRequestDto.getPassword());

        // 회원정보 저장 (추후 동시성 문제 처리 - 이메일 중복 확인 동시에 들어와서 처리 안된 경우)
        User user = User.builder()
                .email(signupRequestDto.getEmail())
                .passwordHash(hashedPassword)
                .name(signupRequestDto.getName())
                .build();
        userRepository.save(user);

        // 반환
        return new SignupResponseDto(user.getEmail(), user.getName());
    }

    // 로그인 로직
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        // 이메일 검증
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 비밀번호 검증
        if(!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // 토큰 발급(추후 코드 변경)
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getName());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // 반환
        return new LoginResponseDto(user.getEmail(), user.getName(), accessToken, refreshToken);
    }
}
