package com.tr.autos.symbol.controller;

import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.symbol.repository.SymbolRepository;
import com.tr.autos.symbol.dto.SymbolDto;
import com.tr.autos.symbol.dto.SymbolLiteDto;
import com.tr.autos.symbol.service.SymbolService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/symbols")
@RequiredArgsConstructor
public class SymbolController {
    private final SymbolService symbolService;
    private final SymbolRepository symbolRepository;

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

    // 전체 리스트(이름순) — 클라이언트 캐싱 전제, 공개 엔드포인트
    @GetMapping("/all")
    public List<SymbolLiteDto> all(@RequestParam String market) {
        return symbolRepository.findAllByMarketOrderByNameAsc(market)
                .stream().map(SymbolLiteDto::from).toList();
    }

    // 특정 종목 조회
    @GetMapping("/{id}")
    public ResponseEntity<SymbolDto> getById(@PathVariable Long id) {
        Symbol symbol = symbolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Symbol not found with id: " + id));
        SymbolDto dto = new SymbolDto(symbol.getId(), symbol.getTicker(), symbol.getMarket(), symbol.getName());
        return ResponseEntity.ok(dto);
    }
}