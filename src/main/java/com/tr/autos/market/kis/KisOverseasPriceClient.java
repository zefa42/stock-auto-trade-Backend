package com.tr.autos.market.kis;

import com.tr.autos.market.kis.dto.KisOverseasPriceDetailResponse;
import com.tr.autos.market.kis.service.KisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class KisOverseasPriceClient {

    private static final String PRICE_DETAIL_PATH = "/uapi/overseas-price/v1/quotations/price-detail";
    private static final String PRICE_DETAIL_TR_ID = "HHDFS76200200";

    private final RestClient restClient;
    private final KisTokenService tokenService;

    @Value("${kis.appkey}")
    private String appKey;

    @Value("${kis.appsecret}")
    private String appSecret;

    @Value("${kis.overseas.auth}")
    private String authCode;

    public KisOverseasPriceDetailResponse fetchPriceDetail(String exchangeCode, String symbol) {
        if (!StringUtils.hasText(authCode)) {
            throw new IllegalStateException("KIS 해외 시세 AUTH 코드가 설정되지 않았습니다. (kis.overseas.auth)");
        }

        String accessToken = tokenService.getAccessTokenOrRefresh();
        try {
            return restClient.get()
                    .uri(builder -> builder
                            .path(PRICE_DETAIL_PATH)
                            .queryParam("AUTH", authCode)
                            .queryParam("EXCD", exchangeCode)
                            .queryParam("SYMB", symbol)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                    .header("authorization", "Bearer " + accessToken)
                    .header("appkey", appKey)
                    .header("appsecret", appSecret)
                    .header("tr_id", PRICE_DETAIL_TR_ID)
                    .header("custtype", "P")
                    .retrieve()
                    .body(KisOverseasPriceDetailResponse.class);
        } catch (RestClientException e) {
            log.error("[KIS] 해외 주식 상세조회 실패 exchange={} symbol={} : {}",
                    exchangeCode, symbol, e.getMessage());
            throw e;
        }
    }
}

