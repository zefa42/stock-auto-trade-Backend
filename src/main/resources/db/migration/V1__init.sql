-- V1__init.sql
-- 초기 스키마 (KRX/US 종목 마스터 = symbols, 관심목록 = watchlists)

-- 안전 설정
SET NAMES utf8mb4;
SET time_zone = 'Asia/Seoul';

-- 1) 사용자
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name          VARCHAR(50)  NOT NULL,
    role          ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) 계좌
CREATE TABLE IF NOT EXISTS accounts (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    broker        VARCHAR(20) DEFAULT 'KIS',
    account_no    VARCHAR(50) NOT NULL,
    account_alias VARCHAR(50),
    linked_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_account (user_id, account_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) 종목 마스터 (숫자 PK + ticker/market 유니크)
CREATE TABLE IF NOT EXISTS symbols (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticker  VARCHAR(20) NOT NULL,     -- 예: '005930', 'AAPL'
    market  VARCHAR(10) NOT NULL,     -- 'KRX' | 'US' (확장 여지)
    name    VARCHAR(100),
    UNIQUE KEY uq_ticker_market (ticker, market),
    KEY idx_symbols_market (market)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4) 관심목록 (user x symbol 중복 금지, 정렬번호/메모 지원)
CREATE TABLE IF NOT EXISTS watchlists (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT   NOT NULL,
    symbol_id  BIGINT   NOT NULL,
    note       VARCHAR(255),
    sort_no    INT      NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_watch_user   FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_watch_symbol FOREIGN KEY (symbol_id) REFERENCES symbols(id) ON DELETE CASCADE,
    UNIQUE KEY uq_watch (user_id, symbol_id),
    KEY idx_watch_user (user_id, sort_no, id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5) 주문 (심볼 FK는 symbol_id로 통일)
CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT      NOT NULL,
    account_id       BIGINT      NOT NULL,
    symbol_id        BIGINT      NOT NULL,
    side             ENUM('BUY','SELL')   NOT NULL,
    type             ENUM('MARKET','LIMIT') NOT NULL,
    price            DECIMAL(18,4),
    qty              INT         NOT NULL,
    status           ENUM('PENDING','PLACED','PARTIAL','FILLED','CANCELED','REJECTED') DEFAULT 'PENDING',
    requested_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    idempotency_key  VARCHAR(100) UNIQUE,
    CONSTRAINT fk_order_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_order_account FOREIGN KEY (account_id)  REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_symbol  FOREIGN KEY (symbol_id)   REFERENCES symbols(id)  ON DELETE CASCADE,
    KEY idx_order_user (user_id, requested_at),
    KEY idx_order_symbol (symbol_id, requested_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6) 체결 내역
CREATE TABLE IF NOT EXISTS executions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT      NOT NULL,
    filled_qty  INT         NOT NULL,
    avg_price   DECIMAL(18,4) NOT NULL,
    executed_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exec_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    KEY idx_exec_order (order_id, executed_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7) 포지션 (user x symbol 유니크)
CREATE TABLE IF NOT EXISTS positions (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT        NOT NULL,
    symbol_id  BIGINT        NOT NULL,
    qty        INT           NOT NULL,
    avg_cost   DECIMAL(18,4) NOT NULL,
    last_pnl   DECIMAL(18,4) DEFAULT 0,
    updated_at TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pos_user   FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_pos_symbol FOREIGN KEY (symbol_id) REFERENCES symbols(id) ON DELETE CASCADE,
    UNIQUE KEY uq_position (user_id, symbol_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8) 일별 손익
CREATE TABLE IF NOT EXISTS daily_returns (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT        NOT NULL,
    date          DATE          NOT NULL,
    pnl_realized  DECIMAL(18,4) DEFAULT 0,
    pnl_unrealized DECIMAL(18,4) DEFAULT 0,
    equity        DECIMAL(18,4) DEFAULT 0,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_daily_return (user_id, date)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9) 캔들 (심볼/시각 유니크)
CREATE TABLE IF NOT EXISTS candles (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol_id BIGINT        NOT NULL,
    ts       DATETIME       NOT NULL,
    o        DECIMAL(18,4)  NOT NULL,
    h        DECIMAL(18,4)  NOT NULL,
    l        DECIMAL(18,4)  NOT NULL,
    c        DECIMAL(18,4)  NOT NULL,
    v        BIGINT         NOT NULL,
    CONSTRAINT fk_candle_symbol FOREIGN KEY (symbol_id) REFERENCES symbols(id) ON DELETE CASCADE,
    UNIQUE KEY uq_candle (symbol_id, ts),
    KEY idx_candle_symbol_time (symbol_id, ts)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10) 기본 심볼 샘플 (KRX + US)
INSERT IGNORE INTO symbols (ticker, market, name) VALUES
('005930','KRX','삼성전자'),
('000660','KRX','SK하이닉스'),
('035420','KRX','NAVER'),
('AAPL','US','Apple Inc.'),
('MSFT','US','Microsoft Corporation'),
('NVDA','US','NVIDIA Corporation');