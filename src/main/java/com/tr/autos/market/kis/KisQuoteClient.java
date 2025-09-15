package com.tr.autos.market.kis;

import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.symbol.repository.SymbolRepository;
import com.tr.autos.market.kis.dto.KisQuoteResponse;
import com.tr.autos.market.kis.service.KisTokenService;
import com.tr.autos.quote.dto.QuoteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisQuoteClient {
    private final RestTemplate restTemplate;
    private final KisTokenService tokenService;
    private final SymbolRepository symbolRepository;

    @Value("${kis.base-url}") private String baseUrl;
    @Value("${kis.appkey}") private String appKey;
    @Value("${kis.appsecret}") private String appSecret;

    public QuoteDto getQuoteBySymbolId(Long symbolId) {
        try {
            Symbol symbol = symbolRepository.findById(symbolId)
                    .orElseThrow(() -> new IllegalArgumentException("Symbol not found: " + symbolId));
            
            return getQuoteByTicker(symbol.getTicker());
        } catch (Exception e) {
            log.error("Failed to get quote for symbolId: {}", symbolId, e);
            throw new RuntimeException("Failed to get quote for symbolId: " + symbolId, e);
        }
    }

    public QuoteDto getQuoteByTicker(String ticker) {
        try {
            String accessToken = tokenService.getAccessToken();
            String url = baseUrl + "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authorization", "Bearer " + accessToken);
            headers.set("appkey", appKey);
            headers.set("appsecret", appSecret);
            headers.set("tr_id", "FHKST03010100");
            
            // 오늘 날짜를 기준으로 전일 데이터 조회
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            String body = String.format("""
                {
                    "fid_cond_mrkt_div_code": "J",
                    "fid_cond_scr_div_code": "20171",
                    "fid_input_iscd": "%s",
                    "fid_input_date_1": "%s",
                    "fid_input_date_2": "%s",
                    "fid_period_div_code": "D",
                    "fid_org_adj_prc": "1"
                }
                """, ticker, today, today);

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            
            log.info("Requesting quote for ticker: {} on date: {}", ticker, today);
            
            ResponseEntity<KisQuoteResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, KisQuoteResponse.class);
            
            KisQuoteResponse quoteResponse = response.getBody();
            
            if (quoteResponse == null || !"0".equals(quoteResponse.getRtCd())) {
                log.error("KIS API error - rt_cd: {}, msg: {}", 
                    quoteResponse != null ? quoteResponse.getRtCd() : "null",
                    quoteResponse != null ? quoteResponse.getMsg1() : "null response");
                throw new RuntimeException("KIS API error: " + 
                    (quoteResponse != null ? quoteResponse.getMsg1() : "null response"));
            }
            
            if (quoteResponse.getOutput() == null || quoteResponse.getOutput().isEmpty()) {
                log.warn("No quote data found for ticker: {}", ticker);
                return createEmptyQuote();
            }
            
            KisQuoteResponse.QuoteData data = quoteResponse.getOutput().get(0);
            return parseQuoteData(data);
            
        } catch (Exception e) {
            log.error("Failed to get quote for ticker: {}", ticker, e);
            throw new RuntimeException("Failed to get quote for ticker: " + ticker, e);
        }
    }

    private QuoteDto parseQuoteData(KisQuoteResponse.QuoteData data) {
        try {
            BigDecimal price = parseBigDecimal(data.getStckPrpr());
            BigDecimal changeAmt = parseBigDecimal(data.getPrdyVrss());
            BigDecimal changeRate = parseBigDecimal(data.getPrdyCtrt());
            BigDecimal prevClose = parseBigDecimal(data.getStckPrdyClpr());
            
            return QuoteDto.builder()
                    .price(price)
                    .changeAmt(changeAmt)
                    .changeRate(changeRate)
                    .prevClose(prevClose)
                    .asOf(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse quote data: {}", data, e);
            return createEmptyQuote();
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.replace(",", ""));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse BigDecimal from: {}", value);
            return BigDecimal.ZERO;
        }
    }

    private QuoteDto createEmptyQuote() {
        return QuoteDto.builder()
                .price(BigDecimal.ZERO)
                .changeAmt(BigDecimal.ZERO)
                .changeRate(BigDecimal.ZERO)
                .prevClose(BigDecimal.ZERO)
                .asOf(LocalDateTime.now())
                .build();
    }
}
