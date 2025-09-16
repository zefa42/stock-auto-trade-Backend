package com.tr.autos.quote.service.updater;

import java.util.Date;

public record QuoteCacheUpsert(
        long price, long prevDiff, Double changeRate, int changeSign,
        Long open, Long high, Long low, Long upperLimit, Long lowerLimit, Long refPrice,
        Long volume, Long amount, Double volumeRateVsPrev, Long foreignNetBuyQty, Long programNetBuyQty,
        Long sharesOutstanding, Long marketCap, Double per, Double pbr, Double eps, Double bps, Double foreignHoldingRatio,
        Long high52w, Date high52wDate, Double high52wDiffRate, Long low52w, Date low52wDate, Double low52wDiffRate,
        String itemStatusCode, boolean creditAllowed, boolean shortSellAllowed,
        String marginRate, String marketWarnCode, boolean tempHalt, String stacMonth
) {}
