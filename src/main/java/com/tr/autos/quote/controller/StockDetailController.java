package com.tr.autos.quote.controller;

import com.tr.autos.domain.quote.dto.StockDetailDto;
import com.tr.autos.quote.service.StockDetailReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockDetailController {
    private final StockDetailReadService readService;

    @GetMapping("/id/{symbolId}/detail")
    public StockDetailDto detailById(@PathVariable Long symbolId) {
        return readService.readFromCache(symbolId);
    }
}
