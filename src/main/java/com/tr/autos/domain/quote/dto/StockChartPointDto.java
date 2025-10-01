package com.tr.autos.domain.quote.dto;

import java.time.LocalDate;

public record StockChartPointDto(
        LocalDate date,
        Long close,
        Long open,
        Long high,
        Long low,
        Long volume,
        Long amount
) {}
