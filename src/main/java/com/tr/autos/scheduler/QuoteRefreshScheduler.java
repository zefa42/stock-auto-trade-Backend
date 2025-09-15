package com.tr.autos.scheduler;

import com.tr.autos.domain.watchlist.repository.WatchlistRepository;
import com.tr.autos.quote.service.QuoteService;
import com.tr.autos.quote.service.QuoteTargetRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuoteRefreshScheduler {
    private final WatchlistRepository watchlistRepository;
    private final QuoteTargetRegistry targetRegistry;
    private final QuoteService quoteService;

    // 예: KRX 장중 갱신, 필요에 맞게 크론 수정
    @Scheduled(cron = "0 */1 9-15 ? * MON-FRI", zone = "Asia/Seoul")
    public void refreshAdminAndExtraTargetsIntraday() {
        runOnce();
    }

    // 예: 정오/마감 갱신
    @Scheduled(cron = "0 0 12,18 ? * MON-FRI", zone = "Asia/Seoul")
    public void refreshNoonAndClose() {
        runOnce();
    }

    private void runOnce() {
        var adminSymbols = new java.util.HashSet<>(watchlistRepository.findDistinctSymbolIdsOfAdmins());
        adminSymbols.addAll(targetRegistry.getExtraTargets()); // 합집합
        for (Long symbolId : adminSymbols) {
            quoteService.refreshSymbolSafe(symbolId);
        }
    }
}
