package com.tr.autos.quote.test;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("local") // local 프로파일에서만 활성화 (application-local.yml 사용 중이죠)
public class StubKisMarketDataClient implements KisMarketDataClient {

    @Override
    public Map<String, QuoteSnapshot> getQuotesByTickers(List<String> tickers) {
        Map<String, QuoteSnapshot> map = new HashMap<>();
        long base = 10000L;

        for (int i = 0; i < tickers.size(); i++) {
            String t = tickers.get(i);

            QuoteSnapshot s = new QuoteSnapshot();
            s.setPrice(base + i * 10);
            s.setPrevDiff(5L);
            s.setChangeRate(0.5);
            s.setChangeSign(1);
            s.setOpenPrice(base + i * 10);
            s.setHighPrice(base + i * 10 + 50);
            s.setLowPrice(base + i * 10 - 50);
            s.setUpperLimit(s.getHighPrice() + 100);
            s.setLowerLimit(s.getLowPrice() - 100);
            s.setRefPrice(base + i * 10 - 5);
            s.setVolume(1000L + i);
            s.setAmount(1_000_000L + i);
            s.setVolumeRateVsPrev(12.34);
            s.setForeignNetBuyQty(100L);
            s.setProgramNetBuyQty(50L);
            s.setSharesOutstanding(1_000_000_000L);
            s.setMarketCap(10_000_000_000L);
            s.setPer(10.5);
            s.setPbr(1.2);
            s.setEps(1234.56);
            s.setBps(7890.12);
            s.setForeignHoldingRatio(8.76);
            s.setHigh52w(base + i * 10 + 500);
            s.setHigh52wDate(Date.valueOf("2025-01-01"));
            s.setHigh52wDiffRate(3.21);
            s.setLow52w(base + i * 10 - 500);
            s.setLow52wDate(Date.valueOf("2024-01-01"));
            s.setLow52wDiffRate(-2.34);
            s.setItemStatusCode("N");
            s.setCreditAllowed(true);
            s.setShortSellAllowed(true);
            s.setMarginRate("30%");
            s.setMarketWarnCode("N");
            s.setTempHalt(false);
            s.setStacMonth("01"); // CHAR(2)/VARCHAR(2)로 맞춰둔 필드
            map.put(t, s);
        }
        return map;
    }
}
