-- V3__add_index_on_symbols_ticker.sql
-- symbols(ticker)에 인덱스가 없으면 생성
SET @idx_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'symbols'
    AND INDEX_NAME = 'idx_symbols_ticker'
);

SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_symbols_ticker ON symbols(ticker)',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;