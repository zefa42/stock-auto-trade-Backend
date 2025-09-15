package com.tr.autos.quote.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuoteTargetRegistry {
    private static final String EXTRA_SET_KEY = "quote:extraTargets";

    private final StringRedisTemplate stringRedisTemplate;

    public void registerExtraTarget(Long symbolId) {
        stringRedisTemplate.opsForSet().add(EXTRA_SET_KEY, String.valueOf(symbolId));
    }

    public void unregisterExtraTargetIfOrphan(Long symbolId,
                                              boolean anyAdminHasIt,
                                              long totalWatchCount) {
        // ADMIN도 안 갖고, 아무 유저도 안 갖고 있으면 제거
        if (!anyAdminHasIt && totalWatchCount == 0) {
            stringRedisTemplate.opsForSet().remove(EXTRA_SET_KEY, String.valueOf(symbolId));
        }
    }

    public Set<Long> getExtraTargets() {
        Set<String> raw = stringRedisTemplate.opsForSet().members(EXTRA_SET_KEY);
        if (raw == null) return Set.of();
        return raw.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toSet());
    }
}
