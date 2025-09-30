package com.tr.autos.market.kis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KisOverseasPriceDetailResponse(
        @JsonProperty("rt_cd") String rtCd,
        @JsonProperty("msg_cd") String msgCd,
        @JsonProperty("msg1") String msg,
        @JsonProperty("output") Output output
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Output(
            @JsonProperty("rsym") String rsym,
            @JsonProperty("pvol") String previousVolume,
            @JsonProperty("open") String open,
            @JsonProperty("high") String high,
            @JsonProperty("low") String low,
            @JsonProperty("last") String last,
            @JsonProperty("base") String base,
            @JsonProperty("tomv") String marketCap,
            @JsonProperty("pamt") String previousAmount,
            @JsonProperty("uplp") String upperLimit,
            @JsonProperty("dnlp") String lowerLimit,
            @JsonProperty("h52p") String high52Week,
            @JsonProperty("h52d") String high52WeekDate,
            @JsonProperty("l52p") String low52Week,
            @JsonProperty("l52d") String low52WeekDate,
            @JsonProperty("perx") String per,
            @JsonProperty("pbrx") String pbr,
            @JsonProperty("epsx") String eps,
            @JsonProperty("bpsx") String bps,
            @JsonProperty("shar") String sharesOutstanding,
            @JsonProperty("mcap") String capital,
            @JsonProperty("curr") String currency,
            @JsonProperty("zdiv") String decimalPlaces,
            @JsonProperty("vnit") String tradingUnit,
            @JsonProperty("t_xprc") String convertedTodayPrice,
            @JsonProperty("t_xdif") String convertedTodayDiff,
            @JsonProperty("t_xrat") String convertedTodayRate,
            @JsonProperty("p_xprc") String convertedPrevPrice,
            @JsonProperty("p_xdif") String convertedPrevDiff,
            @JsonProperty("p_xrat") String convertedPrevRate,
            @JsonProperty("t_rate") String todayFxRate,
            @JsonProperty("p_rate") String prevFxRate,
            @JsonProperty("t_xsgn") String convertedTodaySign,
            @JsonProperty("p_xsng") String convertedPrevSign,
            @JsonProperty("e_ordyn") String tradable,
            @JsonProperty("e_hogau") String priceTickUnit,
            @JsonProperty("e_icod") String sectorCode,
            @JsonProperty("e_parp") String parValue,
            @JsonProperty("tvol") String todayVolume,
            @JsonProperty("tamt") String todayAmount,
            @JsonProperty("etyp_nm") String etpTypeName
    ) {}
}

