package com.tr.autos.market.dto;

import java.math.BigDecimal;

/**
 * 국내 시가총액 상위 30개 종목 정보를 노출용으로 가공한 DTO.
 */
public record DomesticMarketCapRankingItem(
        int rank,
        Long symbolId,
        String market,
        String ticker,
        String name,
        long currentPrice,
        long priceChange,
        String priceChangeSign,
        BigDecimal priceChangeRate,
        long accumulatedVolume,
        long listedShareCount,
        long marketCap,
        BigDecimal marketCapRatio
) {
}

