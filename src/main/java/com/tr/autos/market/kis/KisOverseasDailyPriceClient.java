package com.tr.autos.market.kis;

import com.tr.autos.market.kis.dto.KisOverseasDailyPriceResponse;
import com.tr.autos.market.kis.service.KisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class KisOverseasDailyPriceClient {

    private static final String DAILY_PRICE_PATH = "/uapi/overseas-price/v1/quotations/dailyprice";
    private static final String TR_ID = "HHDFS76240000";
    private static final DateTimeFormatter BYMD_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final RestClient restClient;
    private final KisTokenService tokenService;

    @Value("${kis.appkey}")
    private String appKey;

    @Value("${kis.appsecret}")
    private String appSecret;

    @Value("${kis.overseas.auth:}")
    private String authCode;

    public KisOverseasDailyPriceResponse fetchDailyPrice(String exchangeCode,
                                                         String symbol,
                                                         String gubn,
                                                         LocalDate baseline,
                                                         String modp,
                                                         String keyb) {
        String accessToken = tokenService.getAccessTokenOrRefresh();
        try {
            return restClient.get()
                    .uri(builder -> {
                        var uriBuilder = builder
                                .path(DAILY_PRICE_PATH)
                                .queryParam("AUTH", authCode == null ? "" : authCode)
                                .queryParam("EXCD", exchangeCode)
                                .queryParam("SYMB", symbol)
                                .queryParam("GUBN", gubn)
                                .queryParam("BYMD", BYMD_FORMAT.format(baseline))
                                .queryParam("MODP", modp);
                        if (keyb != null && !keyb.isBlank()) {
                            uriBuilder.queryParam("KEYB", keyb);
                        }
                        return uriBuilder.build();
                    })
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                    .header("authorization", "Bearer " + accessToken)
                    .header("appkey", appKey)
                    .header("appsecret", appSecret)
                    .header("tr_id", TR_ID)
                    .header("custtype", "P")
                    .retrieve()
                    .body(KisOverseasDailyPriceResponse.class);
        } catch (RestClientException e) {
            log.error("[KIS] 해외 기간별 시세 조회 실패 exchange={} symbol={} reason={}",
                    exchangeCode, symbol, e.getMessage());
            throw e;
        }
    }
}

