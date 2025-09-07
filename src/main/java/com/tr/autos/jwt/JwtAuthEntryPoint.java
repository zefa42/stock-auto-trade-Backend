package com.tr.autos.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tr.autos.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res,
                         AuthenticationException authException) throws IOException {
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setContentType("application/json;charset=UTF-8");

        String msg = (String) req.getAttribute("authError");
        if (msg == null) msg = "인증이 필요합니다.";

        var body = ErrorResponse.builder()
                .code("UNAUTHORIZED")
                .message(msg)
                .path(req.getRequestURI())
                .timestamp(Instant.now())
                .details(null)
                .build();

        res.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
