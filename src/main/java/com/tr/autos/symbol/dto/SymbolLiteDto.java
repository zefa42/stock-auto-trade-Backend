package com.tr.autos.symbol.dto;

import com.tr.autos.domain.symbol.Symbol;

// 주식 종목 전체 리스트에 사용
public record SymbolLiteDto(Long id, String ticker, String market, String name) {
    public static SymbolLiteDto from(Symbol s) {
        return new SymbolLiteDto(s.getId(), s.getTicker(), s.getMarket(), s.getName());
    }
}
