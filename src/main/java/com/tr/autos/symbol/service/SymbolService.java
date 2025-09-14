package com.tr.autos.symbol.service;

import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.symbol.repository.SymbolRepository;
import com.tr.autos.symbol.dto.SymbolDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SymbolService {
    private final SymbolRepository symbolRepository;

    public Page<SymbolDto> search(String market, String q, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Symbol> p;
        if(q==null || q.isBlank()){
            p = symbolRepository.findByMarketIgnoreCase(market, pageable);
        }else{
            p = symbolRepository
                    .findByMarketIgnoreCaseAndNameContainingIgnoreCaseOrMarketIgnoreCaseAndTickerContainingIgnoreCase(
                            market, q, market, q, pageable);
        }
        return p.map(s -> new SymbolDto(s.getId(), s.getTicker(), s.getMarket(), s.getName()));
    }
}