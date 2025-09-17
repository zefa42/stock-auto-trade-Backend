package com.tr.autos.quote.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Profile("local")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminRefreshController {
    private final QuoteRefreshService quoteRefreshService;

    // 관심종목 합집합 기준으로 KIS 호출 + quote_cache 갱신
    @PostMapping("/quotes/refresh")
    public ResponseEntity<?> refreshAll() {
        try {
            log.info("Starting quote refresh for all watchlist symbols");
            int updated = quoteRefreshService.refreshAll();
            log.info("Quote refresh completed. Updated {} quotes", updated);
            
            return ResponseEntity.ok().body(
                    java.util.Map.of(
                            "status", "OK", 
                            "updated", updated,
                            "message", "Quote refresh completed successfully"
                    )
            );
        } catch (Exception e) {
            log.error("Failed to refresh quotes", e);
            return ResponseEntity.internalServerError().body(
                    java.util.Map.of(
                            "status", "ERROR",
                            "message", "Failed to refresh quotes: " + e.getMessage()
                    )
            );
        }
    }
}
