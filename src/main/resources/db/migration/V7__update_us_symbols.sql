-- V7: 해외 주식 종목 업데이트

-- 기존 종목 제거
DELETE FROM symbols WHERE ticker IN ('BMNR', 'BLK', 'LLY', 'IONQ', 'JOBY', 'LAC', 'ORCL', 'CRM', 'SNOW', 'VRT') AND market = 'US';

-- 새로운 종목 추가
INSERT IGNORE INTO symbols (ticker, market, name) VALUES
  ('PLTR','US','Palantir Technologies Inc.'),
  ('VKTX','US','Viking Therapeutics, Inc.'),
  ('AMD','US','Advanced Micro Devices, Inc.'),
  ('FSLR','US','First Solar, Inc.'),
  ('UDMY','US','Udemy, Inc.'),
  ('QUBT','US','Quantum Computing Inc.'),
  ('MDB','US','MongoDB, Inc.');

