package com.tr.autos.quote.dto;

import com.tr.autos.domain.quote.QuoteCache;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class QuoteDto {
    private BigDecimal price;        // 현재가
    private BigDecimal changeAmt;    // 전일대비
    private BigDecimal changeRate;   // 전일대비율
    private BigDecimal prevClose;    // 전일종가
    private LocalDateTime asOf;      // 시세 기준 시각

    public QuoteCache toEntity(Long symbolId) {
        return QuoteCache.builder()
                .symbolId(symbolId)
                .price(price != null ? price : BigDecimal.ZERO)
                .changeAmt(changeAmt != null ? changeAmt : BigDecimal.ZERO)
                .changeRate(changeRate != null ? changeRate : BigDecimal.ZERO)
                .prevClose(prevClose != null ? prevClose : BigDecimal.ZERO)
                .asOf(asOf != null ? asOf : LocalDateTime.now())
                .build();
    }
}
