-- users 테이블에 role 컬럼 추가 (기본값 USER)
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';