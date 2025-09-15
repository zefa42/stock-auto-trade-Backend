package com.tr.autos.domain.watchlist.repository;

import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.user.User;
import com.tr.autos.domain.watchlist.Watchlist;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByUserOrderByCreatedAtDesc(User user);
    Optional<Watchlist> findByUserAndSymbol(User user, Symbol symbol);
    void deleteByUserAndSymbol(User user, Symbol symbol);

    // ADMIN 사용자들이 담은 종목만 distinct
    @Query("""
        select distinct w.symbol.id
        from Watchlist w
        join w.user u
        where u.role = 'ADMIN'
    """)
    List<Long> findDistinctSymbolIdsOfAdmins();

    // 해당 종목을 담고 있는 유저 수
    @Query("select count(w) from Watchlist w where w.symbol.id = :symbolId")
    long countBySymbolId(@Param("symbolId") Long symbolId);

    // ADMIN이 이 종목을 담고 있는가?
    @Query("""
        select (count(w) > 0)
        from Watchlist w
        join w.user u
        where w.symbol.id = :symbolId
          and u.role = 'ADMIN'
    """)
    boolean existsBySymbolIdAndAdmin(@Param("symbolId") Long symbolId);

}