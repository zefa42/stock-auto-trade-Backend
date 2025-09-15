package com.tr.autos.quote.controller;

import com.tr.autos.domain.quote.QuoteCache;
import com.tr.autos.domain.quote.repository.QuoteCacheRepository;
import com.tr.autos.quote.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {
    private final QuoteService quoteService;
    private final QuoteCacheRepository quoteCacheRepository;

    @GetMapping("/{symbolId}")
    public ResponseEntity<QuoteCache> getQuote(@PathVariable Long symbolId) {
        return quoteCacheRepository.findBySymbolId(symbolId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<QuoteCache>> getAllQuotes() {
        List<QuoteCache> quotes = quoteCacheRepository.findAll();
        return ResponseEntity.ok(quotes);
    }

    @PostMapping("/refresh/{symbolId}")
    public ResponseEntity<String> refreshQuote(@PathVariable Long symbolId) {
        try {
            quoteService.refreshSymbolAsync(symbolId);
            return ResponseEntity.ok("Quote refresh initiated for symbolId: " + symbolId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to refresh quote: " + e.getMessage());
        }
    }
}
