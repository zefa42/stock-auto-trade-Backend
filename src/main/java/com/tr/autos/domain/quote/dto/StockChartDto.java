package com.tr.autos.domain.quote.dto;

import java.time.LocalDate;
import java.util.List;

public record StockChartDto(
        Long symbolId,
        String ticker,
        String market,
        LocalDate from,
        LocalDate to,
        String period,
        List<StockChartPointDto> points
) {}
