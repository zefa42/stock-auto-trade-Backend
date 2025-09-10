-- V1__init.sql
-- 초기 스키마 (최신 설계 반영)
-- - users.role 추가 (DEFAULT 'USER')
-- - symbols 테이블 삭제 대신 stocks(symbol VARCHAR PK)
-- - 참조컬럼 symbol_id → symbol (문자열)
-- - 가격/금액 정밀도 DECIMAL(18,4)
-- - 기본 인덱스 포함

CREATE DATABASE IF NOT EXISTS auto_trading
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE auto_trading;

-- 사용자
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       name VARCHAR(50) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER',    -- USER / ADMIN
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

-- 종목 마스터 (문자열 티커를 PK로)
CREATE TABLE stocks (
                        symbol VARCHAR(12) PRIMARY KEY,             -- 예: '005930', 'AAPL'
                        market VARCHAR(10) NOT NULL,                -- 'KOSPI','KOSDAQ','NASDAQ' 등
                        name   VARCHAR(100) NOT NULL
);

-- 관심목록 (사용자별 중복 방지)
CREATE TABLE watchlists (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            symbol  VARCHAR(12) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_watch_user   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_watch_symbol FOREIGN KEY (symbol)  REFERENCES stocks(symbol) ON DELETE CASCADE,
                            UNIQUE KEY uq_watch (user_id, symbol)
);

-- 주문
CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        account_id BIGINT NOT NULL,
                        symbol VARCHAR(12) NOT NULL,
                        side ENUM('BUY','SELL') NOT NULL,
                        type ENUM('MARKET','LIMIT') NOT NULL,
                        price DECIMAL(18,4),                                -- 정밀도 상향
                        qty INT NOT NULL,
                        status ENUM('PENDING','PLACED','PARTIAL','FILLED','CANCELED','REJECTED') DEFAULT 'PENDING',
                        requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        idempotency_key VARCHAR(100) UNIQUE,
                        CONSTRAINT fk_order_user    FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
                        CONSTRAINT fk_order_account FOREIGN KEY (account_id)REFERENCES accounts(id)ON DELETE CASCADE,
                        CONSTRAINT fk_order_symbol  FOREIGN KEY (symbol)    REFERENCES stocks(symbol) ON DELETE CASCADE,
                        INDEX idx_order_user (user_id, requested_at),
                        INDEX idx_orders_symbol_time (symbol, requested_at)
);

-- 체결 내역
CREATE TABLE executions (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            order_id BIGINT NOT NULL,
                            filled_qty INT NOT NULL,
                            avg_price DECIMAL(18,4) NOT NULL,                  -- 정밀도 상향
                            executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_exec_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 포지션
CREATE TABLE positions (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           symbol VARCHAR(12) NOT NULL,
                           qty INT NOT NULL,
                           avg_cost DECIMAL(18,4) NOT NULL,                   -- 정밀도 상향
                           last_pnl DECIMAL(18,4) DEFAULT 0,                  -- 정밀도 상향
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           CONSTRAINT fk_pos_user   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                           CONSTRAINT fk_pos_symbol FOREIGN KEY (symbol)  REFERENCES stocks(symbol) ON DELETE CASCADE,
                           UNIQUE KEY uq_position (user_id, symbol)
);

-- 일별 손익
CREATE TABLE daily_returns (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               date DATE NOT NULL,
                               pnl_realized   DECIMAL(18,4) DEFAULT 0,            -- 정밀도 상향
                               pnl_unrealized DECIMAL(18,4) DEFAULT 0,            -- 정밀도 상향
                               equity         DECIMAL(18,4) DEFAULT 0,            -- 정밀도 상향
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_dr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               UNIQUE KEY uq_daily_return (user_id, date)
);

-- 캔들 차트 데이터
CREATE TABLE candles (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         symbol VARCHAR(12) NOT NULL,
                         ts DATETIME NOT NULL,
                         o DECIMAL(18,4) NOT NULL,
                         h DECIMAL(18,4) NOT NULL,
                         l DECIMAL(18,4) NOT NULL,
                         c DECIMAL(18,4) NOT NULL,
                         v BIGINT NOT NULL,
                         CONSTRAINT fk_candle_symbol FOREIGN KEY (symbol) REFERENCES stocks(symbol) ON DELETE CASCADE,
                         UNIQUE KEY uq_candle (symbol, ts),
                         INDEX idx_candles_symbol_ts (symbol, ts)
);

-- 자주 쓰는 조회 인덱스
CREATE INDEX idx_watch_user_created ON watchlists(user_id, created_at);