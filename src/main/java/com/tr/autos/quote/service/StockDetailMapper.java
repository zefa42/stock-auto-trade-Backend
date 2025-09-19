package com.tr.autos.quote.service;

import com.tr.autos.domain.quote.QuoteCache;
import com.tr.autos.domain.quote.dto.StockDetailDto;
import com.tr.autos.domain.symbol.Symbol;

import java.time.ZoneId;

public final class StockDetailMapper {
    private StockDetailMapper(){}
    
    public static StockDetailDto toDto(Symbol s, QuoteCache q) {
        // null 체크
        if (s == null || q == null) {
            throw new IllegalArgumentException("Symbol and QuoteCache cannot be null");
        }
        
        return new StockDetailDto(
                s.getId(), 
                s.getTicker(), 
                s.getMarket(), 
                s.getName(),
                q.getPrice(), 
                q.getPrevDiff(), 
                q.getChangeRate(),
                q.getChangeSign() != null ? q.getChangeSign() : 0, // 기본값 0
                q.getOpenPrice(), 
                q.getHighPrice(), 
                q.getLowPrice(),
                q.getUpperLimit(), 
                q.getLowerLimit(), 
                q.getRefPrice(),
                q.getVolume(), 
                q.getAmount(), 
                q.getVolumeRateVsPrev(),
                q.getForeignNetBuyQty(), 
                q.getProgramNetBuyQty(),
                q.getSharesOutstanding(), 
                q.getMarketCap(),
                q.getPer(), 
                q.getPbr(), 
                q.getEps(), 
                q.getBps(), 
                q.getForeignHoldingRatio(),
                q.getHigh52w(), 
                q.getHigh52wDate() != null ? q.getHigh52wDate().toLocalDate() : null,
                q.getHigh52wDiffRate(),
                q.getLow52w(), 
                q.getLow52wDate() != null ? q.getLow52wDate().toLocalDate() : null,
                q.getLow52wDiffRate(),
                q.getItemStatusCode(), 
                q.getCreditAllowed(), 
                q.getShortSellAllowed(),
                q.getMarginRate(), 
                q.getMarketWarnCode(), 
                q.getTempHalt(), 
                q.getStacMonth(),
                q.getUpdatedAt() != null ? 
                    q.getUpdatedAt().toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime() : 
                    null
        );
    }
}
