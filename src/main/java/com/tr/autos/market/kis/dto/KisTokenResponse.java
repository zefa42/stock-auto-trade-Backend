package com.tr.autos.market.kis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type")   String tokenType,
        @JsonProperty("expires_in")   Long   expiresIn // seconds
) {}
