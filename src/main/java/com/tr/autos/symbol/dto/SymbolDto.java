package com.tr.autos.symbol.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SymbolDto {
    private Long id;
    private String ticker;
    private String market;
    private String name;
}
