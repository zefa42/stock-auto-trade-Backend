package com.tr.autos.domain.quote.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StockDetailDto(
        Long symbolId, String ticker, String market, String name,
        long price, long prevDiff, Double changeRate, int changeSign,
        Long open, Long high, Long low, Long upperLimit, Long lowerLimit, Long refPrice,
        Long volume, Long amount, Double volumeRateVsPrev, Long foreignNetBuyQty, Long programNetBuyQty,
        Long sharesOutstanding, Long marketCap, Double per, Double pbr, Double eps, Double bps, Double foreignHoldingRatio,
        Long high52w, LocalDate high52wDate, Double high52wDiffRate,
        Long low52w, LocalDate low52wDate, Double low52wDiffRate,
        String itemStatusCode, Boolean creditAllowed, Boolean shortSellAllowed,
        String marginRate, String marketWarnCode, Boolean tempHalt, String stacMonth,
        LocalDateTime updatedAt
) {}
