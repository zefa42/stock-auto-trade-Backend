package com.tr.autos.quote.service;

import com.tr.autos.quote.client.KisQuoteClient;
import com.tr.autos.quote.repository.QuoteCacheUpsertRepository;
import com.tr.autos.quote.service.updater.QuoteCacheUpsert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuoteDailyUpdateService {
    private final SymbolPoolProvider pool;
    private final KisQuoteClient kis;
    private final QuoteCacheUpsertRepository upsert;

    public void runOnce() {
        for (var s : pool.listTargets()) {
            try {
                // 당장은 코스피 기본 "J" 사용 (코스닥은 "Q")
                Map<String,Object> res = kis.inquirePriceKr(s.ticker(), "J");
                Map<String,String> o = (Map<String,String>) res.get("output");
                if (o == null) continue;

                int sign = "2".equals(o.get("prdy_vrss_sign")) ? 1
                        : "1".equals(o.get("prdy_vrss_sign")) ? -1 : 0;

                QuoteCacheUpsert u = new QuoteCacheUpsert(
                        L(o.get("stck_prpr")), L(o.get("prdy_vrss")), Dn(o.get("prdy_ctrt")), sign,
                        Ln(o.get("stck_oprc")), Ln(o.get("stck_hgpr")), Ln(o.get("stck_lwpr")),
                        Ln(o.get("stck_mxpr")), Ln(o.get("stck_llam")), Ln(o.get("stck_sdpr")),
                        Ln(o.get("acml_vol")), Ln(o.get("acml_tr_pbmn")), Dn(o.get("prdy_vrss_vol_rate")),
                        Ln(o.get("frgn_ntby_qty")), Ln(o.get("pgtr_ntby_qty")),
                        Ln(o.get("lstn_stcn")), Ln(o.get("hts_avls")),
                        Dn(o.get("per")), Dn(o.get("pbr")), Dn(o.get("eps")), Dn(o.get("bps")), Dn(o.get("hts_frgn_ehrt")),
                        Ln(o.get("w52_hgpr")), toDate(o.get("w52_hgpr_date")), Dn(o.get("w52_hgpr_vrss_prpr_ctrt")),
                        Ln(o.get("w52_lwpr")), toDate(o.get("w52_lwpr_date")), Dn(o.get("w52_lwpr_vrss_prpr_ctrt")),
                        o.get("iscd_stat_cls_code"),
                        "Y".equalsIgnoreCase(o.get("crdt_able_yn")),
                        "Y".equalsIgnoreCase(o.get("ssts_yn")),
                        o.get("marg_rate"),
                        o.get("mrkt_warn_cls_code"),
                        "Y".equalsIgnoreCase(o.get("temp_stop_yn")),
                        o.get("stac_month")
                );
                upsert.upsert(s.id(), u);
            } catch (Exception e) {
                log.warn("daily update failed for {} {}: {}", s.market(), s.ticker(), e.toString());
            }
        }
    }

    private static long L(String s){ return Long.parseLong(s.replaceAll("[^0-9-]","")); }
    private static Long Ln(String s){ return (s==null||s.isBlank())?null:L(s); }
    private static Double D(String s){ return Double.parseDouble(s.replaceAll("[^0-9.-]","")); }
    private static Double Dn(String s){ return (s==null||s.isBlank())?null:D(s); }
    private static Date toDate(String yyyymmdd){
        if (yyyymmdd==null||yyyymmdd.isBlank()) return null;
        return Date.valueOf(LocalDate.of(
                Integer.parseInt(yyyymmdd.substring(0,4)),
                Integer.parseInt(yyyymmdd.substring(4,6)),
                Integer.parseInt(yyyymmdd.substring(6,8))
        ));
    }
}
