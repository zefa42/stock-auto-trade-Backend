package com.tr.autos.quote.test;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
public class QuoteSnapshot {
    private Long price;
    private Long prevDiff;
    private Double changeRate;
    private Integer changeSign;
    private Long openPrice;
    private Long highPrice;
    private Long lowPrice;
    private Long upperLimit;
    private Long lowerLimit;
    private Long refPrice;
    private Long volume;
    private Long amount;
    private Double volumeRateVsPrev;
    private Long foreignNetBuyQty;
    private Long programNetBuyQty;
    private Long sharesOutstanding;
    private Long marketCap;
    private Double per;
    private Double pbr;
    private Double eps;
    private Double bps;
    private Double foreignHoldingRatio;
    private Long high52w;
    private Date high52wDate;
    private Double high52wDiffRate;
    private Long low52w;
    private Date low52wDate;
    private Double low52wDiffRate;
    private String itemStatusCode;
    private Boolean creditAllowed;
    private Boolean shortSellAllowed;
    private String marginRate;
    private String marketWarnCode;
    private Boolean tempHalt;
    private String stacMonth;
}
