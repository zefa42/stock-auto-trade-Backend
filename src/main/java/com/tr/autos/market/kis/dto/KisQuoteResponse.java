package com.tr.autos.market.kis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KisQuoteResponse {
    @JsonProperty("rt_cd")
    private String rtCd;  // 응답코드 (0: 성공)
    
    @JsonProperty("msg_cd")
    private String msgCd; // 메시지코드
    
    @JsonProperty("msg1")
    private String msg1;  // 메시지
    
    @JsonProperty("output")
    private List<QuoteData> output;

    @Data
    public static class QuoteData {
        @JsonProperty("stck_prpr")      // 현재가
        private String stckPrpr;
        
        @JsonProperty("prdy_vrss")      // 전일대비
        private String prdyVrss;
        
        @JsonProperty("prdy_ctrt")      // 전일대비율
        private String prdyCtrt;
        
        @JsonProperty("stck_prdy_clpr") // 전일종가
        private String stckPrdyClpr;
        
        @JsonProperty("acml_vol")       // 누적거래량
        private String acmlVol;
        
        @JsonProperty("acml_tr_pbmn")   // 누적거래대금
        private String acmlTrPbmn;
    }
}
