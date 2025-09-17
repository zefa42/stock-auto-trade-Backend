package com.tr.autos.quote.test;

import org.springframework.stereotype.Service;

public interface QuoteRefreshService {
    /** 관심목록 합집합 → KIS 호출 → quote_cache 일괄 upsert, 갱신 건수 반환 */
    int refreshAll();
}
