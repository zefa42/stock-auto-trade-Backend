-- V2__quote_cache.sql
CREATE TABLE IF NOT EXISTS quote_cache (
    symbol_id BIGINT NOT NULL PRIMARY KEY,
    price DECIMAL(18,4) NOT NULL,   -- 현재가 (stck_prpr)
    change_amt  DECIMAL(18,4) NOT NULL,   -- 전일대비 (prdy_vrss)
    change_rate DECIMAL(9,4)  NOT NULL,   -- 전일대비율 % (prdy_ctrt)
    prev_close  DECIMAL(18,4) NOT NULL,   -- 전일종가 (stck_prdy_clpr)
    as_of       DATETIME     NOT NULL,    -- 시세 기준 시각 (KST→UTC 변환해도 ok)
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_qc_symbol FOREIGN KEY (symbol_id) REFERENCES symbols(id) ON DELETE CASCADE
    );
CREATE INDEX idx_quote_cache_asof ON quote_cache (as_of);