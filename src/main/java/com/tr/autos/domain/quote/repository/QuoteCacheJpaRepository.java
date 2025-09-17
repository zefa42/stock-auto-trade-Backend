package com.tr.autos.domain.quote.repository;

import com.tr.autos.domain.quote.QuoteCache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface QuoteCacheJpaRepository extends JpaRepository<QuoteCache, Long> {
    List<QuoteCache> findBySymbolIdIn(Collection<Long> ids);
    
    // Pagination support
    Page<QuoteCache> findAll(Pageable pageable);
    
    // Find by symbol ID with pagination
    Page<QuoteCache> findBySymbolIdIn(Collection<Long> ids, Pageable pageable);
    
    // Find recent quotes (last updated)
    @Query("SELECT q FROM QuoteCache q ORDER BY q.updatedAt DESC")
    Page<QuoteCache> findRecentQuotes(Pageable pageable);
    
    // Count total quotes
    @Query("SELECT COUNT(q) FROM QuoteCache q")
    long countAllQuotes();
}
