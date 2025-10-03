package com.tr.autos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {
    @Bean
    public RestClient restClient(RestClient.Builder builder,
                                 @Value("${kis.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
