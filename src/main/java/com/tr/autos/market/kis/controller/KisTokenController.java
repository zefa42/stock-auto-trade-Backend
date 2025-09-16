package com.tr.autos.market.kis.controller;

import com.tr.autos.market.kis.service.KisTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/kis/token")
public class KisTokenController {
    private final StringRedisTemplate redis;
    private final KisTokenService tokenService;

    @Value("${kis.token.redis-key}") private String TOKEN_KEY;

    @GetMapping
    public ResponseEntity<?> status() {
        String token = redis.opsForValue().get(TOKEN_KEY);
        Long ttl = redis.getExpire(TOKEN_KEY);
        return ResponseEntity.ok(Map.of(
                "cached", token != null,
                "ttlSec", ttl
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> manualRefresh() {
        String token = tokenService.getAccessTokenOrRefresh();
        return ResponseEntity.ok(Map.of("tokenPrefix", token.substring(0, Math.min(12, token.length())) + "..."));
    }
}
