-- V1__init.sql
-- 초기 스키마 생성

-- 사용자
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       name VARCHAR(50) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 계좌
CREATE TABLE accounts (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          broker VARCHAR(20) DEFAULT 'KIS',
                          account_no VARCHAR(50) NOT NULL,
                          account_alias VARCHAR(50),
                          linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                          UNIQUE KEY uq_account (user_id, account_no)
);

-- 종목 마스터
CREATE TABLE symbols (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         ticker VARCHAR(20) NOT NULL,
                         market VARCHAR(10) NOT NULL,
                         name VARCHAR(100),
                         UNIQUE KEY uq_ticker_market (ticker, market)
);

-- 관심목록
CREATE TABLE watchlists (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            symbol_id BIGINT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_watch_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_watch_symbol FOREIGN KEY (symbol_id) REFERENCES symbols(id) ON DELETE CASCADE,
                            UNIQUE KEY uq_watch (user_id, symbol_id)
);

-- 주문
CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        account_id BIGINT NOT NULL,
                        symbol_id BIGINT NOT NULL,
                        side ENUM('BUY','SELL') NOT NULL,
                        type ENUM('MARKET','LIMIT') NOT NULL,
                        price DECIMAL(15,2),
                        qty INT NOT NULL,
                        status ENUM('PENDING','PLACED','PARTIAL','FILLED','CANCELED','REJECTED') DEFAULT 'PENDING',
                        requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        idempotency_key VARCHAR(100) UNIQUE,
                        CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        CONSTRAINT fk_order_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
                        CONSTRAINT fk_order_symbol FOREIGN KEY (symbol_id) REFERENCES symbols(id) ON DELETE CASCADE,
                        INDEX idx_order_user (user_id, requested_at)
);

-- 체결 내역
CREATE TABLE executions (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            order_id BIGINT NOT NULL,
                            filled_qty INT NOT NULL,
                            avg_price DECIMAL(15,2) NOT NULL,
                            executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_exec_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 포지션
CREATE TABLE positions (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           symbol_id BIGINT NOT NULL,
                           qty INT NOT NULL,
                           avg_cost DECIMAL(15,2) NOT NULL,
                           last_pnl DECIMAL(15,2) DEFAULT 0,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           CONSTRAINT fk_pos_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                           CONSTRAINT fk_pos_symbol FOREIGN KEY (symbol_id) REFERENCES symbols(id) ON DELETE CASCADE,
                           UNIQUE KEY uq_position (user_id, symbol_id)
);

-- 일별 손익
CREATE TABLE daily_returns (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               date DATE NOT NULL,
                               pnl_realized DECIMAL(15,2) DEFAULT 0,
                               pnl_unrealized DECIMAL(15,2) DEFAULT 0,
                               equity DECIMAL(15,2) DEFAULT 0,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_dr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               UNIQUE KEY uq_daily_return (user_id, date)
);

-- (옵션) 캔들 차트 데이터
CREATE TABLE candles (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         symbol_id BIGINT NOT NULL,
                         ts DATETIME NOT NULL,
                         o DECIMAL(15,2) NOT NULL,
                         h DECIMAL(15,2) NOT NULL,
                         l DECIMAL(15,2) NOT NULL,
                         c DECIMAL(15,2) NOT NULL,
                         v BIGINT NOT NULL,
                         CONSTRAINT fk_candle_symbol FOREIGN KEY (symbol_id) REFERENCES symbols(id) ON DELETE CASCADE,
                         UNIQUE KEY uq_candle (symbol_id, ts)
);