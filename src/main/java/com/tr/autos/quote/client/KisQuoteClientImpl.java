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
        String accessToken = tokenService.getAccessTokenOrRefresh(); // 네 메서드명에 맞게 수정
        return restClient.get()
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
    }
}
