package com.tr.autos.config;

import com.tr.autos.jwt.JwtAuthEntryPoint;
import com.tr.autos.jwt.JwtAuthenticationFilter;
import com.tr.autos.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // 모든 도메인 허용 (개발용)
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 쿠키 허용
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/logout"
                        ).permitAll() // 회원가입, 로그인, 종목 검색은 누구나 접근 가능
                        .requestMatchers(HttpMethod.GET,
                                "/api/symbols/**").permitAll()
                        .requestMatchers("/api/watchlist/**").authenticated()
                        .anyRequest().authenticated() // 이외에 인증 필요
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthEntryPoint)) // 등록
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}