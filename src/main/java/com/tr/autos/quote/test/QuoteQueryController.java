package com.tr.autos.quote.test;

import com.tr.autos.domain.quote.QuoteCache;
import com.tr.autos.domain.quote.dto.AdminQuoteDto;
import com.tr.autos.domain.quote.repository.QuoteCacheJpaRepository;
import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.symbol.repository.SymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile("local")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class QuoteQueryController {
    private final QuoteCacheJpaRepository quoteRepo;
    private final SymbolRepository symbolRepo;

    // Admin endpoint: GET /admin/quotes?limit=20&page=0
    @GetMapping("/quotes")
    public ResponseEntity<?> getQuotes(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int page) {
        try {
            log.info("Fetching quotes with limit={}, page={}", limit, page);
            
            // Validate parameters
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest().body(
                        java.util.Map.of("error", "Limit must be between 1 and 100")
                );
            }
            if (page < 0) {
                return ResponseEntity.badRequest().body(
                        java.util.Map.of("error", "Page must be >= 0")
                );
            }

            Pageable pageable = PageRequest.of(page, limit, Sort.by("updatedAt").descending());
            Page<QuoteCache> quotePage = quoteRepo.findAll(pageable);
            
            // QuoteCache를 AdminQuoteDto로 변환 (종목 이름 포함)
            List<AdminQuoteDto> adminQuotes = quotePage.getContent().stream()
                    .map(quote -> {
                        Symbol symbol = symbolRepo.findById(quote.getSymbolId()).orElse(null);
                        return AdminQuoteDto.from(quote, symbol);
                    })
                    .toList();
            
            return ResponseEntity.ok().body(
                    java.util.Map.of(
                            "quotes", adminQuotes,
                            "totalElements", quotePage.getTotalElements(),
                            "totalPages", quotePage.getTotalPages(),
                            "currentPage", quotePage.getNumber(),
                            "size", quotePage.getSize(),
                            "hasNext", quotePage.hasNext(),
                            "hasPrevious", quotePage.hasPrevious()
                    )
            );
        } catch (Exception e) {
            log.error("Failed to fetch quotes", e);
            return ResponseEntity.internalServerError().body(
                    java.util.Map.of("error", "Failed to fetch quotes: " + e.getMessage())
            );
        }
    }

    // 예) /admin/quotes?tickers=AAPL,GOOG
    @GetMapping("/quotes/by-tickers")
    public ResponseEntity<?> getByTickers(@RequestParam(required = false) List<String> tickers) {
        try {
            if (tickers == null || tickers.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        java.util.Map.of("error", "Tickers parameter is required")
                );
            }
            
            log.info("Fetching quotes for tickers: {}", tickers);
            List<Symbol> symbols = symbolRepo.findByTickerIn(tickers);
            List<Long> ids = symbols.stream().map(Symbol::getId).toList();
            List<QuoteCache> quotes = ids.isEmpty() ? List.of() : quoteRepo.findBySymbolIdIn(ids);
            
            // QuoteCache를 AdminQuoteDto로 변환 (종목 이름 포함)
            List<AdminQuoteDto> adminQuotes = quotes.stream()
                    .map(quote -> {
                        Symbol symbol = symbols.stream()
                                .filter(s -> s.getId().equals(quote.getSymbolId()))
                                .findFirst()
                                .orElse(null);
                        return AdminQuoteDto.from(quote, symbol);
                    })
                    .toList();
            
            return ResponseEntity.ok().body(
                    java.util.Map.of(
                            "quotes", adminQuotes,
                            "count", adminQuotes.size(),
                            "requestedTickers", tickers,
                            "foundSymbols", symbols.size()
                    )
            );
        } catch (Exception e) {
            log.error("Failed to fetch quotes by tickers", e);
            return ResponseEntity.internalServerError().body(
                    java.util.Map.of("error", "Failed to fetch quotes: " + e.getMessage())
            );
        }
    }

    // 예) /admin/quotes/symbol/123
    @GetMapping("/quotes/symbol/{symbolId}")
    public ResponseEntity<?> getOne(@PathVariable Long symbolId) {
        try {
            log.info("Fetching quote for symbol ID: {}", symbolId);
            
            QuoteCache quote = quoteRepo.findById(symbolId).orElse(null);
            if (quote == null) {
                return ResponseEntity.notFound().build();
            }
            
            Symbol symbol = symbolRepo.findById(symbolId).orElse(null);
            AdminQuoteDto adminQuote = AdminQuoteDto.from(quote, symbol);
            
            return ResponseEntity.ok(adminQuote);
        } catch (Exception e) {
            log.error("Failed to fetch quote for symbol ID: {}", symbolId, e);
            return ResponseEntity.internalServerError().body(
                    java.util.Map.of("error", "Failed to fetch quote: " + e.getMessage())
            );
        }
    }
}
