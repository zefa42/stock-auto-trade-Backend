package com.tr.autos.market.kis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KisDailyChartPriceResponse(
        @JsonProperty("rt_cd") String rtCd,
        @JsonProperty("msg_cd") String msgCd,
        @JsonProperty("msg1") String msg,
        @JsonProperty("output1") Output1 output1,
        @JsonProperty("output2") List<Output2> output2
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Output1(
            @JsonProperty("hts_kor_isnm") String koreanName,
            @JsonProperty("stck_shrn_iscd") String shortCode,
            @JsonProperty("stck_prdy_clpr") String prevClose,
            @JsonProperty("stck_prpr") String currentPrice
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Output2(
            @JsonProperty("stck_bsop_date") String businessDate,
            @JsonProperty("stck_clpr") String closePrice,
            @JsonProperty("stck_oprc") String openPrice,
            @JsonProperty("stck_hgpr") String highPrice,
            @JsonProperty("stck_lwpr") String lowPrice,
            @JsonProperty("acml_vol") String accumulatedVolume,
            @JsonProperty("acml_tr_pbmn") String accumulatedTransactionAmount,
            @JsonProperty("prdy_vrss") String prevDiff,
            @JsonProperty("prdy_vrss_sign") String prevDiffSign,
            @JsonProperty("mod_yn") String modified
    ) {}
}
