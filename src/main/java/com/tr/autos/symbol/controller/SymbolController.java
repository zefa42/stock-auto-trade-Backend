package com.tr.autos.symbol.controller;

import com.tr.autos.symbol.dto.SymbolDto;
import com.tr.autos.symbol.service.SymbolService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/symbols")
@RequiredArgsConstructor
public class SymbolController {
    private final SymbolService symbolService;

    // 예: GET /api/symbols?market=KRX&q=삼성&page=0&size=20
    @GetMapping
    public ResponseEntity<Page<SymbolDto>> search(
            @RequestParam String market,
            @RequestParam(required=false) String q,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size
    ){
        return ResponseEntity.ok(symbolService.search(market, q, page, size));
    }
}