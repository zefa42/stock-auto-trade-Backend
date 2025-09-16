package com.tr.autos.quote.client;

import java.util.Map;

public interface KisQuoteClient {
    Map<String, Object> inquirePriceKr(String ticker, String mrktDivCode); // "J"(코스피) / "Q"(코스닥)
}