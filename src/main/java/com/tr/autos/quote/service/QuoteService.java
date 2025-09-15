package com.tr.autos.quote.service;

import com.tr.autos.market.kis.KisAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuoteService {
    private final KisAuthClient kisAuthClient;
    private final QuoteCacheRepository cacheRepo;

    private final java.util.Set<Long> inFlight = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @org.springframework.scheduling.annotation.Async
    public void refreshSymbolAsync(Long symbolId) {
        refreshSymbolSafe(symbolId);
    }

    public void refreshSymbolSafe(Long symbolId) {
        if (!inFlight.add(symbolId)) return;
        try {
            var dto = kisAuthClient.getQuoteBySymbolId(symbolId); // 한투 시세 1회 호출
            cacheRepo.save(dto.toEntity(symbolId));           // 캐시 반영
        } finally {
            inFlight.remove(symbolId);
        }
    }
}
