package com.tr.autos.quote.scheduler;

import com.tr.autos.quote.service.QuoteDailyUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuoteDailyScheduler {
    private final QuoteDailyUpdateService svc;

    @Scheduled(cron = "${quotes.daily.cron:0 0 18 * * MON-FRI}", zone = "Asia/Seoul")
    public void run() {
        log.info("QuoteDailyScheduler start");
        svc.runOnce();
        log.info("QuoteDailyScheduler end");
    }
}
