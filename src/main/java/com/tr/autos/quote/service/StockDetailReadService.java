package com.tr.autos.quote.service;

import com.tr.autos.domain.quote.QuoteCache;
import com.tr.autos.domain.quote.dto.StockDetailDto;
import com.tr.autos.domain.quote.repository.QuoteCacheJpaRepository;
import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.symbol.repository.SymbolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StockDetailReadService {
    private final SymbolRepository symbolRepo;
    private final QuoteCacheJpaRepository quoteRepo;

    @Transactional(readOnly = true)
    public StockDetailDto readFromCache(Long symbolId) {
        Symbol s = symbolRepo.findById(symbolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "symbol not found"));
        QuoteCache q = quoteRepo.findById(symbolId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "quote not cached yet"));
        return StockDetailMapper.toDto(s, q);
    }
}
