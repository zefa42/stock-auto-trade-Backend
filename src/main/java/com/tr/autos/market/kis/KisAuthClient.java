package com.tr.autos.market.kis;

import com.tr.autos.market.kis.dto.KisTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KisAuthClient {
    private final RestTemplate restTemplate;

    @Value("${kis.base-url}") private String baseUrl;
    @Value("${kis.appkey}")   private String appKey;
    @Value("${kis.appsecret}")private String appSecret;

    /** KIS 액세스 토큰 발급 */
    public KisTokenResponse issueAccessToken() {
        String url = baseUrl + "/oauth2/tokenP";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
          {
            "grant_type": "client_credentials",
            "appkey": "%s",
            "appsecret": "%s"
          }
        """.formatted(appKey, appSecret);

        ResponseEntity<KisTokenResponse> res = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), KisTokenResponse.class);
        return res.getBody();
    }

    /** KIS 액세스 토큰 폐기 */
    public void revokeAccessToken(String accessToken) {
        String url = baseUrl + "/oauth2/revokeP";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
          {
            "appkey": "%s",
            "appsecret": "%s",
            "token": "%s"
          }
        """.formatted(appKey, appSecret, accessToken);

        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Void.class);
    }
}