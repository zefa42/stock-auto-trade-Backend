package com.tr.autos.watchlist.service;

import com.tr.autos.domain.symbol.repository.SymbolRepository;
import com.tr.autos.domain.user.User;
import com.tr.autos.domain.user.repository.UserRepository;
import com.tr.autos.domain.watchlist.Watchlist;
import com.tr.autos.watchlist.dto.WatchItemDto;
import com.tr.autos.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {
    private final UserRepository userRepository;
    private final SymbolRepository symbolRepository;
    private final WatchlistRepository watchlistRepository;

    private User currentUser(Principal principal){
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    // readonly 수정
    @Transactional
    public List<WatchItemDto> myList(Principal principal){
        var user = currentUser(principal);
        return watchlistRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(w -> {
                    var s = w.getSymbol();
                    return new WatchItemDto(s.getId(), s.getTicker(), s.getMarket(), s.getName());
                })
                .toList();
    }

    @Transactional
    public void add(Principal principal, Long symbolId){
        var user = currentUser(principal);
        var symbol = symbolRepository.findById(symbolId).orElseThrow();
        watchlistRepository.findByUserAndSymbol(user, symbol).ifPresent(w -> {
            throw new IllegalArgumentException("이미 관심목록에 있습니다.");
        });
        watchlistRepository.save(Watchlist.builder().user(user).symbol(symbol).build());
    }

    @Transactional
    public void remove(Principal principal, Long symbolId){
        var user = currentUser(principal);
        var symbol = symbolRepository.findById(symbolId).orElseThrow();
        watchlistRepository.deleteByUserAndSymbol(user, symbol);
    }
}