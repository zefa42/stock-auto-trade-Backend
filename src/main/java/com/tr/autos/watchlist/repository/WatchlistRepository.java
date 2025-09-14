package com.tr.autos.watchlist.repository;

import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.user.User;
import com.tr.autos.domain.watchlist.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByUserOrderByCreatedAtDesc(User user);
    Optional<Watchlist> findByUserAndSymbol(User user, Symbol symbol);
    void deleteByUserAndSymbol(User user, Symbol symbol);
}