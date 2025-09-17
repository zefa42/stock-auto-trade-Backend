package com.tr.autos.quote.service;

import com.tr.autos.domain.quote.QuoteCache;
import com.tr.autos.domain.quote.repository.QuoteCacheJpaRepository;
import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.quote.client.KisQuoteClient;
import com.tr.autos.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisIntegrationService {

    private final WatchlistRepository watchlistRepository;
    private final QuoteCacheJpaRepository quoteCacheRepository;
    private final KisQuoteClient kisQuoteClient;

    @Transactional
    public int refreshAllQuotes() {
        log.info("Starting KIS quote refresh for all watchlist symbols");
        
        // 1) Get distinct symbols from all watchlists
        List<Symbol> distinctSymbols = watchlistRepository.findDistinctSymbols();
        if (distinctSymbols.isEmpty()) {
            log.info("No symbols found in watchlists");
            return 0;
        }

        log.info("Found {} distinct symbols in watchlists", distinctSymbols.size());

        // 2) Map symbols by ticker
        Map<String, Symbol> symbolByTicker = distinctSymbols.stream()
                .collect(Collectors.toMap(Symbol::getTicker, s -> s, (a, b) -> a));

        List<String> fetchableTickers = new ArrayList<>(symbolByTicker.keySet());
        List<QuoteCache> upserts = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        // 3) Fetch quotes from KIS API
        for (String ticker : fetchableTickers) {
            try {
                String mrktDivCode = determineMarketDivisionCode(ticker);
                Map<String, Object> response = kisQuoteClient.inquirePriceKr(ticker, mrktDivCode);
                
                if (response != null && response.containsKey("output")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> output = (Map<String, Object>) response.get("output");
                    QuoteCache quoteCache = mapKisResponseToQuoteCache(output, symbolByTicker.get(ticker));
                    upserts.add(quoteCache);
                    successCount++;
                    log.debug("Successfully fetched quote for ticker: {}", ticker);
                } else {
                    log.warn("No output data in KIS response for ticker: {}", ticker);
                    errorCount++;
                }
            } catch (Exception e) {
                log.error("Failed to fetch quote for ticker: {}, error: {}", ticker, e.getMessage());
                errorCount++;
            }
        }

        // 4) Save all quotes
        if (!upserts.isEmpty()) {
            quoteCacheRepository.saveAll(upserts);
            log.info("Successfully saved {} quotes to database", upserts.size());
        }

        log.info("KIS quote refresh completed. Success: {}, Errors: {}", successCount, errorCount);
        return successCount;
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

    private QuoteCache mapKisResponseToQuoteCache(Map<String, Object> output, Symbol symbol) {
        QuoteCache quoteCache = new QuoteCache();
        Timestamp now = Timestamp.from(Instant.now());

        try {
            // Map KIS API response fields to QuoteCache
            quoteCache.setSymbolId(symbol.getId());
            quoteCache.setPrice(parseLong(output.get("stck_prpr")));
            quoteCache.setPrevDiff(parseLong(output.get("prdy_vrss")));
            quoteCache.setChangeRate(parseDouble(output.get("prdy_ctrt")));
            quoteCache.setChangeSign(parseInteger(output.get("prdy_vrss_sign")));
            quoteCache.setOpenPrice(parseLong(output.get("stck_oprc")));
            quoteCache.setHighPrice(parseLong(output.get("stck_hgpr")));
            quoteCache.setLowPrice(parseLong(output.get("stck_lwpr")));
            quoteCache.setUpperLimit(parseLong(output.get("stck_sdpr")));
            quoteCache.setLowerLimit(parseLong(output.get("stck_sdpr")));
            quoteCache.setRefPrice(parseLong(output.get("stck_fcam")));
            quoteCache.setVolume(parseLong(output.get("acml_vol")));
            quoteCache.setAmount(parseLong(output.get("acml_tr_pbmn")));
            quoteCache.setVolumeRateVsPrev(parseDouble(output.get("vol_tnrt")));
            quoteCache.setForeignNetBuyQty(parseLong(output.get("frgn_ntby_qty")));
            quoteCache.setProgramNetBuyQty(parseLong(output.get("pgm_ntby_qty")));
            quoteCache.setSharesOutstanding(parseLong(output.get("lstn_stcn")));
            quoteCache.setMarketCap(parseLong(output.get("hts_avls")));
            quoteCache.setPer(parseDouble(output.get("per")));
            quoteCache.setPbr(parseDouble(output.get("pbr")));
            quoteCache.setEps(parseDouble(output.get("eps")));
            quoteCache.setBps(parseDouble(output.get("bps")));
            quoteCache.setForeignHoldingRatio(parseDouble(output.get("frgn_hlnd")));
            quoteCache.setHigh52w(parseLong(output.get("w52_hgpr")));
            quoteCache.setHigh52wDate(parseDate(output.get("w52_hgpr_dt")));
            quoteCache.setHigh52wDiffRate(parseDouble(output.get("w52_hgpr_dt")));
            quoteCache.setLow52w(parseLong(output.get("w52_lwpr")));
            quoteCache.setLow52wDate(parseDate(output.get("w52_lwpr_dt")));
            quoteCache.setLow52wDiffRate(parseDouble(output.get("w52_lwpr_dt")));
            quoteCache.setItemStatusCode(parseString(output.get("item_status_code")));
            quoteCache.setCreditAllowed(parseBoolean(output.get("credit_allowed")));
            quoteCache.setShortSellAllowed(parseBoolean(output.get("short_sell_allowed")));
            quoteCache.setMarginRate(parseString(output.get("margin_rate")));
            quoteCache.setMarketWarnCode(parseString(output.get("market_warn_code")));
            quoteCache.setTempHalt(parseBoolean(output.get("temp_halt")));
            quoteCache.setStacMonth(parseString(output.get("stac_month")));
            quoteCache.setUpdatedAt(now);

        } catch (Exception e) {
            log.error("Error mapping KIS response to QuoteCache for symbol {}: {}", symbol.getTicker(), e.getMessage());
            throw new RuntimeException("Failed to map KIS response", e);
        }

        return quoteCache;
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

    private java.sql.Date parseDate(Object value) {
        if (value == null) return null;
        try {
            String dateStr = value.toString();
            if (dateStr.length() == 8) {
                // Format: YYYYMMDD
                int year = Integer.parseInt(dateStr.substring(0, 4));
                int month = Integer.parseInt(dateStr.substring(4, 6));
                int day = Integer.parseInt(dateStr.substring(6, 8));
                return java.sql.Date.valueOf(String.format("%04d-%02d-%02d", year, month, day));
            }
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", value);
        }
        return null;
    }
}
