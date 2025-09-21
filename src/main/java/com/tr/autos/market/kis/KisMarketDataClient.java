package com.tr.autos.market.kis;

import com.tr.autos.market.kis.dto.KisDailyChartPriceResponse;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class KisMarketDataClient {

    private static final String DAILY_CHART_PATH = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RestClient restClient;
    private final KisTokenService tokenService;

    @Value("${kis.appkey}")
    private String appKey;

    @Value("${kis.appsecret}")
    private String appSecret;

    public KisDailyChartPriceResponse fetchDailyChart(String symbolCode,
                                                       LocalDate from,
                                                       LocalDate to,
                                                       String periodDivCode,
                                                       String marketDivCode,
                                                       String orgAdjPriceFlag) {
        String accessToken = tokenService.getAccessTokenOrRefresh();
        try {
            return restClient.get()
                    .uri(builder -> builder
                            .path(DAILY_CHART_PATH)
                            .queryParam("FID_COND_MRKT_DIV_CODE", marketDivCode)
                            .queryParam("FID_INPUT_ISCD", symbolCode)
                            .queryParam("FID_INPUT_DATE_1", DATE_FORMATTER.format(from))
                            .queryParam("FID_INPUT_DATE_2", DATE_FORMATTER.format(to))
                            .queryParam("FID_PERIOD_DIV_CODE", periodDivCode)
                            .queryParam("FID_ORG_ADJ_PRC", orgAdjPriceFlag)
                            .build())
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                    .header("authorization", "Bearer " + accessToken)
                    .header("appkey", appKey)
                    .header("appsecret", appSecret)
                    .header("tr_id", "FHKST03010100")
                    .header("custtype", "P")
                    .retrieve()
                    .body(KisDailyChartPriceResponse.class);
        } catch (RestClientException e) {
            log.error("[KIS] 일별 차트 조회 실패 symbol={} from={} to={} : {}", symbolCode, from, to, e.getMessage());
            throw e;
        }
    }
}
