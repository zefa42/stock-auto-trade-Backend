package com.tr.autos.quote.test;

import java.util.List;
import java.util.Map;


public interface KisMarketDataClient {
    /**
     * 주어진 티커들의 현재 시세 스냅샷을 반환.
     * @return key = ticker
     */
    Map<String, QuoteSnapshot> getQuotesByTickers(List<String> tickers);
}
