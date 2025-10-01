package com.tr.autos.market.kis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KisOverseasDailyPriceResponse(
        @JsonProperty("rt_cd") String rtCd,
        @JsonProperty("msg_cd") String msgCd,
        @JsonProperty("msg1") String msg,
        @JsonProperty("output1") Output1 output1,
        @JsonProperty("output2") List<Output2> output2
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Output1(
            @JsonProperty("rsym") String rsym,
            @JsonProperty("zdiv") String decimalPlaces,
            @JsonProperty("nrec") String previousClose,
            @JsonProperty("keyb") String keyBuffer
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Output2(
            @JsonProperty("xymd") String businessDate,
            @JsonProperty("clos") String closePrice,
            @JsonProperty("open") String openPrice,
            @JsonProperty("high") String highPrice,
            @JsonProperty("low") String lowPrice,
            @JsonProperty("tvol") String accumulatedVolume,
            @JsonProperty("tamt") String accumulatedTransactionAmount
    ) {}
}

