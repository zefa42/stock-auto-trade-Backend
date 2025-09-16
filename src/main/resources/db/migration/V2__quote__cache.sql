-- V2__quote_cache.sql
CREATE TABLE IF NOT EXISTS quote_cache (
    symbol_id              BIGINT      NOT NULL PRIMARY KEY,
    price                  BIGINT      NOT NULL,
    prev_diff              BIGINT      DEFAULT 0,
    change_rate            DECIMAL(6,2),
    change_sign            TINYINT,
    open_price             BIGINT,
    high_price             BIGINT,
    low_price              BIGINT,
    upper_limit            BIGINT,
    lower_limit            BIGINT,
    ref_price              BIGINT,
    volume                 BIGINT,
    amount                 BIGINT,
    volume_rate_vs_prev    DECIMAL(8,2),
    foreign_net_buy_qty    BIGINT,
    program_net_buy_qty    BIGINT,
    shares_outstanding     BIGINT,
    market_cap             BIGINT,
    per                    DECIMAL(10,2),
    pbr                    DECIMAL(10,2),
    eps                    DECIMAL(18,4),
    bps                    DECIMAL(18,4),
    foreign_holding_ratio  DECIMAL(8,2),
    high52w                BIGINT,
    high52w_date           DATE,
    high52w_diff_rate      DECIMAL(8,2),
    low52w                 BIGINT,
    low52w_date            DATE,
    low52w_diff_rate       DECIMAL(8,2),
    item_status_code       VARCHAR(3),
    credit_allowed         BOOLEAN,
    short_sell_allowed     BOOLEAN,
    margin_rate            VARCHAR(16),
    market_warn_code       VARCHAR(4),
    temp_halt              BOOLEAN,
    stac_month             CHAR(2),
    updated_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_quote_symbol FOREIGN KEY (symbol_id)
    REFERENCES symbols(id) ON DELETE CASCADE
    );

-- (선택) ticker로 탐색이 잦다면
CREATE INDEX IF NOT EXISTS idx_symbols_ticker ON symbols(ticker);