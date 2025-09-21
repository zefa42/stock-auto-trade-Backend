package com.tr.autos.quote.controller;

import com.tr.autos.domain.quote.dto.StockChartDto;
import com.tr.autos.domain.quote.dto.StockDetailDto;
import com.tr.autos.quote.service.StockChartService;
import com.tr.autos.quote.service.StockDetailReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockDetailController {
    private final StockDetailReadService readService;
    private final StockChartService chartService;

    @GetMapping("/id/{symbolId}/detail")
    public StockDetailDto detailById(@PathVariable Long symbolId) {
        return readService.readFromCache(symbolId);
    }

    @GetMapping("/id/{symbolId}/chart")
    public StockChartDto chartById(@PathVariable Long symbolId,
                                   @RequestParam(value = "from", required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam(value = "to", required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                   @RequestParam(value = "period", defaultValue = "D") String period,
                                   @RequestParam(value = "adjusted", defaultValue = "true") boolean adjusted) {
        return chartService.getDailyChart(symbolId, from, to, period, adjusted);
    }
}
