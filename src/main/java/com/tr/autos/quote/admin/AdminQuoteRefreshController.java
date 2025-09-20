package com.tr.autos.quote.admin;

import com.tr.autos.quote.service.KisIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@RestController
@RequestMapping("/admin/quotes")
@RequiredArgsConstructor
@Slf4j
public class AdminQuoteRefreshController {

    private final KisIntegrationService kisIntegrationService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAllNow() {
        long start = System.currentTimeMillis();
        try {
            int updated = kisIntegrationService.refreshAllQuotes();
            long took = System.currentTimeMillis() - start;
            log.info("[Admin] manual quote refresh completed updated={} took={}ms", updated, took);
            return ResponseEntity.ok(Map.of(
                    "updated", updated,
                    "tookMs", took,
                    "timestamp", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString()
            ));
        } catch (Exception e) {
            long took = System.currentTimeMillis() - start;
            log.error("[Admin] manual quote refresh failed took={}ms: {}", took, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to refresh quotes",
                    "message", e.getMessage(),
                    "tookMs", took,
                    "timestamp", ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString()
            ));
        }
    }
}
