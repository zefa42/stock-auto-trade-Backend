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

    @Scheduled(cron = "${quotes.daily.cron.morning:0 0 9 * * MON-FRI}", zone = "Asia/Seoul")
    public void runMorningOpen() {
        runJob("09:00");
    }

    @Scheduled(cron = "${quotes.daily.cron.midday:0 0 12 * * MON-FRI}", zone = "Asia/Seoul")
    public void runMiddayCheck() {
        runJob("12:00");
    }

    @Scheduled(cron = "${quotes.daily.cron.closing:0 31 15 * * MON-FRI}", zone = "Asia/Seoul")
    public void runAfterClosingBell() {
        runJob("15:31");
    }

    @Scheduled(cron = "${quotes.daily.cron.afterHours:0 5 18 * * MON-FRI}", zone = "Asia/Seoul")
    public void runAfterHours() {
        runJob("18:05");
    }

    private void runJob(String windowLabel) {
        log.info("QuoteDailyScheduler start [{}]", windowLabel);
        try {
            svc.runOnce();
            log.info("QuoteDailyScheduler end [{}]", windowLabel);
        } catch (Exception e) {
            log.error("QuoteDailyScheduler failed [{}]: {}", windowLabel, e.getMessage(), e);
            throw e;
        }
    }
}
