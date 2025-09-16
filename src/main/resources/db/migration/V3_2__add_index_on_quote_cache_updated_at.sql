-- V3_2__add_index_on_quote_cache_updated_at.sql
-- quote_cache(updated_at)에 인덱스가 없으면 생성

SET @idx_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'quote_cache'
    AND INDEX_NAME = 'idx_quote_cache_updated_at'
);

SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_quote_cache_updated_at ON quote_cache(updated_at)',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;