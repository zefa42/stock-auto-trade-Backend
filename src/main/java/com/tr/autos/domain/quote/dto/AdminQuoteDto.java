package com.tr.autos.domain.quote.dto;

import com.tr.autos.domain.quote.QuoteCache;
import com.tr.autos.domain.symbol.Symbol;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/**
 * Admin 페이지용 Quote DTO
 * 종목 이름과 업데이트 시간을 포함
 */
public record AdminQuoteDto(
    Long symbolId,
    String ticker,
    String symbolName,
    String market,
    Long price,
    Long prevDiff,
    Double changeRate,
    Integer changeSign,
    Long volume,
    Long marketCap,
    String updatedAt,
    String timeAgo
) {
    public static AdminQuoteDto from(QuoteCache quote, Symbol symbol) {
        return new AdminQuoteDto(
            quote.getSymbolId(),
            symbol.getTicker(),
            symbol.getName(),
            symbol.getMarket(),
            quote.getPrice(),
            quote.getPrevDiff(),
            quote.getChangeRate(),
            quote.getChangeSign(),
            quote.getVolume(),
            quote.getMarketCap(),
            formatTimestamp(quote.getUpdatedAt()),
            formatTimeAgo(quote.getUpdatedAt())
        );
    }
    
    private static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        
        // MySQL에 저장된 시간이 이미 한국 시간이므로 그대로 사용
        return timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    private static String formatTimeAgo(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        
        // MySQL에 저장된 시간이 이미 한국 시간이므로 그대로 사용
        java.time.LocalDateTime storedTime = timestamp.toLocalDateTime();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(storedTime, now);
        
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        
        if (days > 0) {
            return days + "일 전";
        } else if (hours > 0) {
            return hours + "시간 전";
        } else if (minutes > 0) {
            return minutes + "분 전";
        } else {
            return "방금 전";
        }
    }
}
