package com.tr.autos.market.kis.service;

import com.tr.autos.market.kis.KisAuthClient;
import com.tr.autos.market.kis.dto.KisTokenResponse;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class KisTokenService {

    private final StringRedisTemplate redis;
    private final KisAuthClient authClient;

    @Value("${kis.token.redis-key}")     private String TOKEN_KEY;
    @Value("${kis.token.safety-seconds}")private long SAFETY_SECONDS;

    /** 애플리케이션에서 KIS API 부를 때 사용 */
    public String getAccessToken() {
        String token = redis.opsForValue().get(TOKEN_KEY);
        if (token != null && !token.isBlank()) return token;

        log.info("[KIS] 토큰 캐시 미존재 → 신규 발급");
        return issueAndCache();
    }

    /** 스케줄러가 주기적으로 TTL 확인 후 갱신 */
    @Scheduled(fixedDelay = 300_000) // 5분마다 점검
    public void refreshIfNeeded() {
        Long ttl = redis.getExpire(TOKEN_KEY);
        if (ttl == null || ttl < 0) {
            // 키 없음 or 만료 시각 모름 → 안전하게 재발급
            log.info("[KIS] 토큰 TTL 없음 → 재발급");
            issueAndCache();
            return;
        }
        if (ttl <= SAFETY_SECONDS) {
            log.info("[KIS] 토큰 TTL={}s, safety={}s 이하 → 조기 갱신", ttl, SAFETY_SECONDS);
            issueAndCache();
        }
    }

    /** 안전 종료 시 현재 토큰 폐기(가능하면) */
    @PreDestroy
    public void onShutdown() {
        Optional.ofNullable(redis.opsForValue().get(TOKEN_KEY))
                .filter(s -> !s.isBlank())
                .ifPresent(token -> {
                    try {
                        authClient.revokeAccessToken(token);
                        log.info("[KIS] 종료 훅: 토큰 폐기 완료");
                    } catch (Exception e) {
                        log.warn("[KIS] 종료 훅: 토큰 폐기 실패(무시): {}", e.getMessage());
                    }
                });
    }

    // ===== 내부 유틸 =====

    private String issueAndCache() {
        KisTokenResponse res = authClient.issueAccessToken();
        if (res == null || res.accessToken() == null) {
            throw new IllegalStateException("KIS 토큰 발급 실패: 응답이 비어있습니다.");
        }
        long ttl = Math.max(1, (res.expiresIn() != null ? res.expiresIn() : 86_400) - SAFETY_SECONDS);
        redis.opsForValue().set(TOKEN_KEY, res.accessToken(), Duration.ofSeconds(ttl));
        log.info("[KIS] 토큰 발급/캐시 완료. TTL={}s (원본:{}s, safety:{}s)", ttl, res.expiresIn(), SAFETY_SECONDS);
        return res.accessToken();
    }
}
