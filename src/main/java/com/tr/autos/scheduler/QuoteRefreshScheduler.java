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

    // 12시 정각 갱신
    @Scheduled(cron = "0 0 12 ? * MON-FRI", zone = "Asia/Seoul")
    public void refreshAtNoon() {
        runOnce();
    }

    // 18시 30분 갱신
    @Scheduled(cron = "0 30 18 ? * MON-FRI", zone = "Asia/Seoul")
    public void refreshAtEvening() {
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
