package com.tr.autos.quote.client;

import com.tr.autos.market.kis.service.KisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KisQuoteClientImpl implements KisQuoteClient {

    private final RestClient restClient;           // RestConfig에서 주입
    private final KisTokenService tokenService;    // 토큰 꺼냄

    @Value("${kis.base-url}")  String baseUrl;
    @Value("${kis.appkey}")    String appKey;
    @Value("${kis.appsecret}") String appSecret;

    private static final String TR_ID = "FHKST01010100";

    @Override @SuppressWarnings("unchecked")
    public Map<String, Object> inquirePriceKr(String ticker, String mrktDivCode) {
        try {
            if (ticker == null || ticker.trim().isEmpty()) {
                throw new IllegalArgumentException("Ticker cannot be null or empty");
            }
            if (mrktDivCode == null || mrktDivCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Market division code cannot be null or empty");
            }

            String accessToken = tokenService.getAccessTokenOrRefresh();
            if (accessToken == null || accessToken.trim().isEmpty()) {
                throw new IllegalStateException("Failed to obtain access token");
            }

            log.debug("Fetching quote for ticker: {}, market: {}", ticker, mrktDivCode);
            
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/inquire-price")
                            .queryParam("fid_cond_mrkt_div_code", mrktDivCode) // J/Q
                            .queryParam("fid_input_iscd", ticker)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header("appkey", appKey)
                    .header("appsecret", appSecret)
                    .header("tr_id", TR_ID)
                    .header("custtype", "P")
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                log.warn("Received null response from KIS API for ticker: {}", ticker);
                return null;
            }

            // Check for error in response
            if (response.containsKey("rt_cd") && !"0".equals(response.get("rt_cd"))) {
                String errorMsg = (String) response.get("msg1");
                log.error("KIS API error for ticker {}: {} - {}", ticker, response.get("rt_cd"), errorMsg);
                throw new RuntimeException("KIS API error: " + errorMsg);
            }

            log.debug("Successfully fetched quote for ticker: {}", ticker);
            return response;

        } catch (Exception e) {
            log.error("Failed to fetch quote for ticker: {}, market: {}, error: {}", 
                     ticker, mrktDivCode, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch quote from KIS API", e);
        }
    }
}
