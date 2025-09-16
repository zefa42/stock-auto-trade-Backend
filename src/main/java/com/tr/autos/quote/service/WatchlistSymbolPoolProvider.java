package com.tr.autos.quote.service;


import com.tr.autos.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WatchlistSymbolPoolProvider implements SymbolPoolProvider {
    private final WatchlistRepository repo;

    @Override
    public List<SymbolBrief> listTargets() {
        return repo.findDistinctSymbols().stream()
                .map(s -> new SymbolBrief(s.getId(), s.getTicker(), s.getMarket()))
                .toList();
    }
}
