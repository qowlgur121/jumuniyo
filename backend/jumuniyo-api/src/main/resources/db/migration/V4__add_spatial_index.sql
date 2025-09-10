-- 공간 인덱스를 위한 마이그레이션
-- 작성자: AI
-- 날짜: 2025-05-26

-- 1. 위치 정보를 저장할 POINT 타입 컬럼 추가
ALTER TABLE stores ADD COLUMN location POINT GENERATED ALWAYS AS 
  (POINT(longitude, latitude)) STORED;

-- 2. 공간 인덱스 생성
CREATE SPATIAL INDEX idx_store_spatial_location ON stores (location);

-- 3. 공간 인덱스 사용을 위한 메타데이터 설정
-- MySQL 8.0 이상에서는 SRID(Spatial Reference ID)가 필요
-- SRID 4326은 WGS84 좌표계 (GPS 좌표)를 의미
ALTER TABLE stores MODIFY COLUMN location POINT SRID 4326; 