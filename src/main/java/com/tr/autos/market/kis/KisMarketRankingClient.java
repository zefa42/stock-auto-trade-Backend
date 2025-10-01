package com.tr.autos.market.kis;

import com.tr.autos.market.kis.dto.KisMarketCapRankingResponse;
import com.tr.autos.market.kis.service.KisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisMarketRankingClient {

    private static final String MARKET_CAP_RANKING_PATH = "/uapi/domestic-stock/v1/ranking/market-cap";

    private final RestClient restClient;
    private final KisTokenService tokenService;

    @Value("${kis.appkey}")
    private String appKey;

    @Value("${kis.appsecret}")
    private String appSecret;

    public KisMarketCapRankingResponse fetchMarketCapRanking() {
        String accessToken = tokenService.getAccessTokenOrRefresh();
        try {
            return restClient.get()
                    .uri(builder -> builder
                            .path(MARKET_CAP_RANKING_PATH)
                            .queryParam("FID_INPUT_PRICE_2", "0")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                            .queryParam("FID_COND_SCR_DIV_CODE", "20174")
                            .queryParam("FID_DIV_CLS_CODE", "0")
                            .queryParam("FID_INPUT_ISCD", "0000")
                            .queryParam("FID_TRGT_CLS_CODE", "0")
                            .queryParam("FID_TRGT_EXLS_CLS_CODE", "0")
                            .queryParam("FID_INPUT_PRICE_1", "0")
                            .queryParam("FID_VOL_CNT", "0")
                            .build())
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                    .header("authorization", "Bearer " + accessToken)
                    .header("appkey", appKey)
                    .header("appsecret", appSecret)
                    .header("tr_id", "FHPST01740000")
                    .header("custtype", "P")
                    .retrieve()
                    .body(KisMarketCapRankingResponse.class);
        } catch (RestClientException e) {
            log.error("[KIS] 국내 시가총액 순위 API 호출 실패: {}", e.getMessage());
            throw e;
        }
    }
}

