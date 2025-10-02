package com.tr.autos.market.kis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * KIS 국내 시가총액 상위 API 응답 DTO.
 */
public record KisMarketCapRankingResponse(
        @JsonProperty("rt_cd") String rtCd,
        @JsonProperty("msg_cd") String msgCd,
        @JsonProperty("msg1") String message,
        @JsonProperty("output") List<Entry> output
) {
    public record Entry(
            @JsonProperty("mksc_shrn_iscd") String mkscShrnIscd,
            @JsonProperty("data_rank") String dataRank,
            @JsonProperty("hts_kor_isnm") String htsKorIsnm,
            @JsonProperty("stck_prpr") String stckPrpr,
            @JsonProperty("prdy_vrss") String prdyVrss,
            @JsonProperty("prdy_vrss_sign") String prdyVrssSign,
            @JsonProperty("prdy_ctrt") String prdyCtrt,
            @JsonProperty("acml_vol") String acmlVol,
            @JsonProperty("lstn_stcn") String lstnStcn,
            @JsonProperty("stck_avls") String stckAvls,
            @JsonProperty("mrkt_whol_avls_rlim") String mrktWholAvlsRlim
    ) {}
}

