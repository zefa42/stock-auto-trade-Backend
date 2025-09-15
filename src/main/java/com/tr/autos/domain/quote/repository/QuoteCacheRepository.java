package com.tr.autos.domain.quote.repository;

import com.tr.autos.domain.quote.QuoteCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuoteCacheRepository extends JpaRepository<QuoteCache, Long> {

    // 특정 심볼의 캐시 찾기
    Optional<QuoteCache> findBySymbolId(Long symbolId);

    // 최신 시세만 보여줄 때 편리
    boolean existsBySymbolId(Long symbolId);
}
