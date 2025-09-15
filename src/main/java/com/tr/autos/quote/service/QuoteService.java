package com.tr.autos.quote.service;

import com.tr.autos.domain.quote.repository.QuoteCacheRepository;
import com.tr.autos.market.kis.KisQuoteClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {
    private final KisQuoteClient kisQuoteClient;
    private final QuoteCacheRepository cacheRepo;

    private final java.util.Set<Long> inFlight = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @org.springframework.scheduling.annotation.Async
    public void refreshSymbolAsync(Long symbolId) {
        refreshSymbolSafe(symbolId);
    }

    public void refreshSymbolSafe(Long symbolId) {
        if (!inFlight.add(symbolId)) {
            log.debug("Quote refresh already in progress for symbolId: {}", symbolId);
            return;
        }
        
        try {
            log.info("Starting quote refresh for symbolId: {}", symbolId);
            var dto = kisQuoteClient.getQuoteBySymbolId(symbolId); // 한투 시세 1회 호출
            cacheRepo.save(dto.toEntity(symbolId));           // 캐시 반영
            log.info("Successfully refreshed quote for symbolId: {} - Price: {}, Change: {}%", 
                symbolId, dto.getPrice(), dto.getChangeRate());
        } catch (Exception e) {
            log.error("Failed to refresh quote for symbolId: {}", symbolId, e);
            // 에러 발생 시 로그만 남기고 재시도는 하지 않음 (추후 구현 예정)
        } finally {
            inFlight.remove(symbolId);
        }
    }
}
