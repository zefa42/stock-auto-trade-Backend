package com.tr.autos.domain.symbol.repository;

import com.tr.autos.domain.symbol.Symbol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SymbolRepository extends JpaRepository<Symbol, Long> {
    Page<Symbol> findByMarketIgnoreCaseAndNameContainingIgnoreCaseOrMarketIgnoreCaseAndTickerContainingIgnoreCase(
            String market1, String nameLike, String market2, String tickerLike, Pageable pageable);

    Page<Symbol> findByMarketIgnoreCase(String market, Pageable pageable);
}