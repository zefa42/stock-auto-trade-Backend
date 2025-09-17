package com.tr.autos.quote.test;

import com.tr.autos.quote.client.KisQuoteClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("!local") // Use this for production/staging environments
@RequiredArgsConstructor
@Slf4j
public class RealKisMarketDataClient implements KisMarketDataClient {

    private final KisQuoteClient kisQuoteClient;

    @Override
    public Map<String, QuoteSnapshot> getQuotesByTickers(List<String> tickers) {
        Map<String, QuoteSnapshot> result = new HashMap<>();
        
        for (String ticker : tickers) {
            try {
                // Determine market division code based on ticker format
                String mrktDivCode = determineMarketDivisionCode(ticker);
                
                // Call KIS API
                Map<String, Object> response = kisQuoteClient.inquirePriceKr(ticker, mrktDivCode);
                
                if (response != null && response.containsKey("output")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> output = (Map<String, Object>) response.get("output");
                    QuoteSnapshot snapshot = mapKisResponseToQuoteSnapshot(output);
                    result.put(ticker, snapshot);
                    log.debug("Successfully fetched quote for ticker: {}", ticker);
                } else {
                    log.warn("No output data in KIS response for ticker: {}", ticker);
                }
            } catch (Exception e) {
                log.error("Failed to fetch quote for ticker: {}, error: {}", ticker, e.getMessage());
                // Continue with other tickers even if one fails
            }
        }
        
        log.info("Successfully fetched quotes for {}/{} tickers", result.size(), tickers.size());
        return result;
    }

    private String determineMarketDivisionCode(String ticker) {
        // For Korean stocks, use "J" for KOSPI, "Q" for KOSDAQ
        // This is a simplified logic - you might want to enhance this based on your data
        if (ticker.matches("\\d{6}")) {
            // 6-digit numeric ticker - assume KOSPI for now
            return "J";
        }
        return "J"; // Default to KOSPI
    }

    private QuoteSnapshot mapKisResponseToQuoteSnapshot(Map<String, Object> output) {
        QuoteSnapshot snapshot = new QuoteSnapshot();
        
        try {
            // Map KIS API response fields to QuoteSnapshot
            snapshot.setPrice(parseLong(output.get("stck_prpr")));
            snapshot.setPrevDiff(parseLong(output.get("prdy_vrss")));
            snapshot.setChangeRate(parseDouble(output.get("prdy_ctrt")));
            snapshot.setChangeSign(parseInteger(output.get("prdy_vrss_sign")));
            snapshot.setOpenPrice(parseLong(output.get("stck_oprc")));
            snapshot.setHighPrice(parseLong(output.get("stck_hgpr")));
            snapshot.setLowPrice(parseLong(output.get("stck_lwpr")));
            snapshot.setUpperLimit(parseLong(output.get("stck_sdpr")));
            snapshot.setLowerLimit(parseLong(output.get("stck_sdpr")));
            snapshot.setRefPrice(parseLong(output.get("stck_fcam")));
            snapshot.setVolume(parseLong(output.get("acml_vol")));
            snapshot.setAmount(parseLong(output.get("acml_tr_pbmn")));
            snapshot.setVolumeRateVsPrev(parseDouble(output.get("vol_tnrt")));
            snapshot.setForeignNetBuyQty(parseLong(output.get("frgn_ntby_qty")));
            snapshot.setProgramNetBuyQty(parseLong(output.get("pgm_ntby_qty")));
            snapshot.setSharesOutstanding(parseLong(output.get("lstn_stcn")));
            snapshot.setMarketCap(parseLong(output.get("hts_avls")));
            snapshot.setPer(parseDouble(output.get("per")));
            snapshot.setPbr(parseDouble(output.get("pbr")));
            snapshot.setEps(parseDouble(output.get("eps")));
            snapshot.setBps(parseDouble(output.get("bps")));
            snapshot.setForeignHoldingRatio(parseDouble(output.get("frgn_hlnd")));
            snapshot.setHigh52w(parseLong(output.get("w52_hgpr")));
            snapshot.setHigh52wDate(parseDate(output.get("w52_hgpr_dt")));
            snapshot.setHigh52wDiffRate(parseDouble(output.get("w52_hgpr_dt")));
            snapshot.setLow52w(parseLong(output.get("w52_lwpr")));
            snapshot.setLow52wDate(parseDate(output.get("w52_lwpr_dt")));
            snapshot.setLow52wDiffRate(parseDouble(output.get("w52_lwpr_dt")));
            snapshot.setItemStatusCode(parseString(output.get("item_status_code")));
            snapshot.setCreditAllowed(parseBoolean(output.get("credit_allowed")));
            snapshot.setShortSellAllowed(parseBoolean(output.get("short_sell_allowed")));
            snapshot.setMarginRate(parseString(output.get("margin_rate")));
            snapshot.setMarketWarnCode(parseString(output.get("market_warn_code")));
            snapshot.setTempHalt(parseBoolean(output.get("temp_halt")));
            snapshot.setStacMonth(parseString(output.get("stac_month")));
            
        } catch (Exception e) {
            log.error("Error mapping KIS response to QuoteSnapshot: {}", e.getMessage());
            throw new RuntimeException("Failed to map KIS response", e);
        }
        
        return snapshot;
    }

    // Helper methods for safe parsing
    private Long parseLong(Object value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(Object value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseString(Object value) {
        return value != null ? value.toString() : null;
    }

    private Boolean parseBoolean(Object value) {
        if (value == null) return null;
        String str = value.toString().toLowerCase();
        return "y".equals(str) || "true".equals(str) || "1".equals(str);
    }

    private Date parseDate(Object value) {
        if (value == null) return null;
        try {
            String dateStr = value.toString();
            if (dateStr.length() == 8) {
                // Format: YYYYMMDD
                int year = Integer.parseInt(dateStr.substring(0, 4));
                int month = Integer.parseInt(dateStr.substring(4, 6));
                int day = Integer.parseInt(dateStr.substring(6, 8));
                return Date.valueOf(String.format("%04d-%02d-%02d", year, month, day));
            }
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", value);
        }
        return null;
    }
}
