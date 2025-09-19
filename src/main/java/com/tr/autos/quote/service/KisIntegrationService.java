package com.tr.autos.quote.service;

import com.tr.autos.domain.quote.QuoteCache;
import com.tr.autos.domain.quote.repository.QuoteCacheJpaRepository;
import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.quote.client.KisQuoteClient;
import com.tr.autos.utils.DataConversionUtils;
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
            // 정답 매핑표에 따른 KIS API 응답 필드 매핑
            quoteCache.setSymbolId(symbol.getId());
            
            // 기본 가격 정보
            quoteCache.setPrice(DataConversionUtils.toLong(output.get("stck_prpr")));
            quoteCache.setPrevDiff(DataConversionUtils.toLong(output.get("prdy_vrss")));
            quoteCache.setChangeRate(DataConversionUtils.toDouble(output.get("prdy_ctrt")));
            quoteCache.setChangeSign(DataConversionUtils.toInt(output.get("prdy_vrss_sign")));
            quoteCache.setOpenPrice(DataConversionUtils.toLong(output.get("stck_oprc")));
            quoteCache.setHighPrice(DataConversionUtils.toLong(output.get("stck_hgpr")));
            quoteCache.setLowPrice(DataConversionUtils.toLong(output.get("stck_lwpr")));
            
            // 상한가/하한가/기준가 (수정된 매핑)
            quoteCache.setUpperLimit(DataConversionUtils.toLong(output.get("stck_mxpr")));
            quoteCache.setLowerLimit(DataConversionUtils.toLong(output.get("stck_llam")));
            quoteCache.setRefPrice(DataConversionUtils.toLong(output.get("stck_sdpr")));
            
            // 거래량/거래대금
            quoteCache.setVolume(DataConversionUtils.toLong(output.get("acml_vol")));
            quoteCache.setAmount(DataConversionUtils.toLong(output.get("acml_tr_pbmn")));
            quoteCache.setVolumeRateVsPrev(DataConversionUtils.toDouble(output.get("prdy_vrss_vol_rate")));
            
            // 외국인/프로그램 매매
            quoteCache.setForeignNetBuyQty(DataConversionUtils.toLong(output.get("frgn_ntby_qty")));
            quoteCache.setProgramNetBuyQty(DataConversionUtils.toLong(output.get("pgtr_ntby_qty")));
            quoteCache.setSharesOutstanding(DataConversionUtils.toLong(output.get("lstn_stcn")));
            
            // 시가총액 (백만원 → 원 단위 변환)
            quoteCache.setMarketCap(DataConversionUtils.normalizeMarketCap(output.get("hts_avls"), "백만원"));
            
            // PER/PBR/EPS/BPS
            quoteCache.setPer(DataConversionUtils.toDouble(output.get("per")));
            quoteCache.setPbr(DataConversionUtils.toDouble(output.get("pbr")));
            quoteCache.setEps(DataConversionUtils.toDouble(output.get("eps")));
            quoteCache.setBps(DataConversionUtils.toDouble(output.get("bps")));
            
            // 외국인 보유비율
            quoteCache.setForeignHoldingRatio(DataConversionUtils.toDouble(output.get("hts_frgn_ehrt")));
            
            // 52주 고가/저가
            quoteCache.setHigh52w(DataConversionUtils.toLong(output.get("w52_hgpr")));
            quoteCache.setHigh52wDate(DataConversionUtils.toSqlDateYYYYMMDD(output.get("w52_hgpr_date")));
            quoteCache.setHigh52wDiffRate(DataConversionUtils.toDouble(output.get("w52_hgpr_vrss_prpr_ctrt")));
            quoteCache.setLow52w(DataConversionUtils.toLong(output.get("w52_lwpr")));
            quoteCache.setLow52wDate(DataConversionUtils.toSqlDateYYYYMMDD(output.get("w52_lwpr_date")));
            quoteCache.setLow52wDiffRate(DataConversionUtils.toDouble(output.get("w52_lwpr_vrss_prpr_ctrt")));
            
            // 기타 정보
            quoteCache.setItemStatusCode(DataConversionUtils.toString(output.get("iscd_stat_cls_code")));
            quoteCache.setCreditAllowed(DataConversionUtils.toBoolYN(output.get("crdt_able_yn")));
            quoteCache.setShortSellAllowed(DataConversionUtils.toBoolYN(output.get("ssts_yn")));
            quoteCache.setMarginRate(DataConversionUtils.toString(output.get("marg_rate")));
            quoteCache.setMarketWarnCode(DataConversionUtils.toString(output.get("mrkt_warn_cls_code")));
            quoteCache.setTempHalt(DataConversionUtils.toBoolYN(output.get("temp_stop_yn")));
            quoteCache.setStacMonth(DataConversionUtils.toString(output.get("stac_month")));
            quoteCache.setUpdatedAt(now);

        } catch (Exception e) {
            log.error("Error mapping KIS response to QuoteCache for symbol {}: {}", symbol.getTicker(), e.getMessage());
            throw new RuntimeException("Failed to map KIS response", e);
        }

        return quoteCache;
    }

}
