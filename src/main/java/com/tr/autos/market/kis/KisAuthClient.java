package com.tr.autos.market.kis;

import com.tr.autos.market.kis.dto.KisTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KisAuthClient {
    private final RestClient restClient;

    @Value("${kis.base-url}") private String baseUrl;
    @Value("${kis.appkey}")   private String appKey;
    @Value("${kis.appsecret}")private String appSecret;

    /** KIS 액세스 토큰 발급 */
    public KisTokenResponse issueAccessToken() {
        return restClient
                .post()
                .uri("/oauth2/tokenP")                   // baseUrl은 RestConfig에서 주입
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "grant_type", "client_credentials",
                        "appkey", appKey,
                        "appsecret", appSecret
                ))
                .retrieve()
                .body(KisTokenResponse.class);
    }

    /** KIS 액세스 토큰 폐기 */
    public void revokeAccessToken(String accessToken) {
        restClient
                .post()
                .uri("/oauth2/revokeP")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "appkey", appKey,
                        "appsecret", appSecret,
                        "token", accessToken
                ))
                .retrieve()
                .toBodilessEntity();                      // 바디 없는 응답
    }
}