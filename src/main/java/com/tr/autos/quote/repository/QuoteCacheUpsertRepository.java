package com.tr.autos.quote.repository;

import com.tr.autos.quote.service.updater.QuoteCacheUpsert;

public interface QuoteCacheUpsertRepository {
    void upsert(Long symbolId, QuoteCacheUpsert u);
}
