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
    @Value("${jwt.issuer}")   private String issuer;
    @Value("${jwt.audience}") private String audience;

    private Key key() {
        // secret는 Base64 문자열. 먼저 디코드해서 바이트 배열로 만든 뒤 키 생성
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** ★ jti 포함 Access 토큰 생성 */
    public String createAccessToken(String email, String name, String jti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(email)
                .claim("name", name)
                .claim("typ", "access")
                .claim("jti", jti)
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtl)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String email, String jti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(email)
                .claim("typ", "refresh")        // ★ refresh 구분
                .claim("jti", jti)
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshTtl)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 파싱하면서 검증 수행
    // 잘못된 서명이거나 만료된 토큰이면 예외를 던짐
    // 파싱 시 iss/aud도 확인(간단 검증)
    public Jws<Claims> parse(String token) {
        var jws = Jwts.parserBuilder()
                .setSigningKey(key())
                .setAllowedClockSkewSeconds(30) // 선택: 시계 오차 허용
                .build()
                .parseClaimsJws(token);

        Claims c = jws.getBody();
        if (!issuer.equals(c.getIssuer()) || (audience != null && !audience.equals(c.getAudience()))) {
            throw new io.jsonwebtoken.JwtException("유효하지 않은 iss/aud");
        }
        return jws;
    }

    // Redis TTL 설정에 사용
    public long refreshTtlSeconds() {
        return refreshTtl;
    }
}
