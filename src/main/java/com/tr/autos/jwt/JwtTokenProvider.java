package com.tr.autos.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final ObjectMapper om = new ObjectMapper();

    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.access-token-valid-seconds}") private long accessTtl;
    @Value("${jwt.refresh-token-valid-seconds}") private long refreshTtl;

    private Key key() {
        // secret는 Base64 문자열. 먼저 디코드해서 바이트 배열로 만든 뒤 키 생성
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(String email, String name) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .claim("name", name)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtl)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .claim("typ", "refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshTtl)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 파싱하면서 검증 수행
    // 잘못된 서명이거나 만료된 토큰이면 예외를 던짐
    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key()).build()
                .parseClaimsJws(token);
    }
}
