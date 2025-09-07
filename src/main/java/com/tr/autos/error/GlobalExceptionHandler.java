package com.tr.autos.error;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String msg,
                                                String path, Map<String, ?> details) {
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .code(code)
                        .message(msg)
                        .path(path)
                        .timestamp(Instant.now())
                        .details(details)
                        .build()
        );
    }

    // (1) @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValid(MethodArgumentNotValidException ex,
                                                     HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream().collect(Collectors.toMap(FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> b));
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "요청 값이 올바르지 않습니다.", req.getRequestURI(), fieldErrors);
    }

    // (2) 비즈니스 검증(지금 서비스에서 던진 IllegalArgumentException 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegal(IllegalArgumentException ex,
                                                       HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(),
                req.getRequestURI(), null);
    }

    // (3) JWT 파싱/만료 등
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwt(JwtException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "토큰이 유효하지 않거나 만료되었습니다.", req.getRequestURI(), null);
    }

    // (4) 권한 부족
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleDenied(AccessDeniedException ex,
                                                      HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "접근 권한이 없습니다.", req.getRequestURI(), null);
    }

    // (5) DB 제약 위반 등
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleData(DataIntegrityViolationException ex,
                                                    HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "CONFLICT",
                "데이터 제약 조건 위반입니다.", req.getRequestURI(), null);
    }

    // (6) 그 밖의 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "서버 오류가 발생했습니다.", req.getRequestURI(), null);
    }
}
