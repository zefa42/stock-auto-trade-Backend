package com.tr.autos.quote.service;

import java.util.List;

public interface SymbolPoolProvider {
    record SymbolBrief(Long id, String ticker, String market){}
    List<SymbolBrief> listTargets();
}
