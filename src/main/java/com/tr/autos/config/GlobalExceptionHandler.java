package com.tr.autos.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// 서비스/컨트롤러 예외는 @ControllerAdvice로 JSON 통일
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> handleBad(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error","BAD_REQUEST","message", e.getMessage()));
    }
}
