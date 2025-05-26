-- 성능 최적화를 위한 인덱스 추가
-- 작성자: AI
-- 날짜: 2025-05-26

-- 1. 위치 기반 검색을 위한 인덱스
CREATE INDEX idx_store_location ON stores (latitude, longitude);

-- 2. 자주 사용되는 필터링 조건에 대한 복합 인덱스
CREATE INDEX idx_store_active_approved ON stores (is_active, is_approved);
CREATE INDEX idx_store_category_active_approved ON stores (category_id, is_active, is_approved);
CREATE INDEX idx_store_rating_active_approved ON stores (rating DESC, is_active, is_approved);
CREATE INDEX idx_store_delivery_fee_active_approved ON stores (delivery_fee ASC, is_active, is_approved);
CREATE INDEX idx_store_review_count_active_approved ON stores (review_count DESC, is_active, is_approved);

-- 3. 검색 기능 향상을 위한 인덱스
CREATE INDEX idx_store_name ON stores (name);
CREATE FULLTEXT INDEX ftx_store_name_description ON stores (name, description);

-- 4. 생성일 기반 필터링 (신규 매장)을 위한 인덱스
CREATE INDEX idx_store_created_at ON stores (created_at DESC);

-- 5. 배달 지역 검색을 위한 인덱스
CREATE INDEX idx_delivery_area_name ON delivery_areas (area_name);
CREATE INDEX idx_delivery_area_active ON delivery_areas (is_active);
CREATE INDEX idx_delivery_area_store ON delivery_areas (store_id);

-- 6. 운영 시간 검색을 위한 인덱스
CREATE INDEX idx_operating_hour_store ON operating_hours (store_id);
CREATE INDEX idx_operating_hour_day_time ON operating_hours (day_of_week, open_time, close_time);

-- 7. 사업자 등록번호 검색을 위한 인덱스 (이미 UNIQUE 제약조건이 있을 수 있음)
CREATE INDEX IF NOT EXISTS idx_store_business_number ON stores (business_number);

-- 8. 최소 주문 금액 필터링을 위한 인덱스
CREATE INDEX idx_store_minimum_order ON stores (minimum_order_amount ASC);

-- 주의: 인덱스가 많으면 INSERT/UPDATE 성능이 저하될 수 있으므로
-- 실제 쿼리 패턴을 모니터링하여 불필요한 인덱스는 제거하는 것이 좋습니다. 