package com.tr.autos.domain.quote.repository;

import com.tr.autos.domain.quote.QuoteCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface QuoteCacheJpaRepository extends JpaRepository<QuoteCache, Long> {
    List<QuoteCache> findBySymbolIdIn(Collection<Long> ids);
}
