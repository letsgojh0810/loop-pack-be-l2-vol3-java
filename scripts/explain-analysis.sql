-- ================================================================
-- 상품 목록 조회 EXPLAIN 분석 스크립트
-- 목적: 인덱스 적용 전후 성능 비교
-- 실행: docker exec -i docker-mysql-1 mysql -u application -papplication loopers < scripts/explain-analysis.sql
-- ================================================================

USE loopers;

-- ================================================================
-- [1] 인덱스 있는 상태 (현재) EXPLAIN 분석
-- ================================================================

SELECT '=== [현재 인덱스 목록] ===' AS '';
SHOW INDEX FROM products;

SELECT '' AS '';
SELECT '=== [CASE 1] 전체 상품 최신순 (idx_products_created_at) ===' AS '';
EXPLAIN SELECT id, name, price, brand_id, like_count, created_at
FROM products
WHERE deleted_at IS NULL
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;

SELECT '' AS '';
SELECT '=== [CASE 2] 전체 상품 가격순 (idx_products_price) ===' AS '';
EXPLAIN SELECT id, name, price, brand_id, like_count, created_at
FROM products
WHERE deleted_at IS NULL
ORDER BY price ASC
LIMIT 20 OFFSET 0;

SELECT '' AS '';
SELECT '=== [CASE 3] 전체 상품 좋아요순 (idx_products_like_count) ===' AS '';
EXPLAIN SELECT id, name, price, brand_id, like_count, created_at
FROM products
WHERE deleted_at IS NULL
ORDER BY like_count DESC
LIMIT 20 OFFSET 0;

SELECT '' AS '';
SELECT '=== [CASE 4] 브랜드 필터 + 최신순 (idx_products_brand_created_at) ===' AS '';
EXPLAIN SELECT id, name, price, brand_id, like_count, created_at
FROM products
WHERE brand_id = 1 AND deleted_at IS NULL
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;

SELECT '' AS '';
SELECT '=== [CASE 5] 브랜드 필터 + 가격순 (idx_products_brand_price) ===' AS '';
EXPLAIN SELECT id, name, price, brand_id, like_count, created_at
FROM products
WHERE brand_id = 1 AND deleted_at IS NULL
ORDER BY price ASC
LIMIT 20 OFFSET 0;

SELECT '' AS '';
SELECT '=== [CASE 6] 브랜드 필터 + 좋아요순 (idx_products_brand_like_count) ===' AS '';
EXPLAIN SELECT id, name, price, brand_id, like_count, created_at
FROM products
WHERE brand_id = 1 AND deleted_at IS NULL
ORDER BY like_count DESC
LIMIT 20 OFFSET 0;

-- ================================================================
-- [2] 인덱스 제거 후 EXPLAIN (풀스캔 비교)
-- ================================================================

SELECT '' AS '';
SELECT '=== [인덱스 제거 후 풀스캔 비교] ===' AS '';

DROP INDEX idx_products_created_at   ON products;
DROP INDEX idx_products_price        ON products;
DROP INDEX idx_products_like_count   ON products;
DROP INDEX idx_products_brand_created_at ON products;
DROP INDEX idx_products_brand_price  ON products;
DROP INDEX idx_products_brand_like_count ON products;

SELECT '' AS '';
SELECT '=== [NO INDEX] 전체 최신순 ===' AS '';
EXPLAIN SELECT id, name, price, brand_id, like_count, created_at
FROM products
WHERE deleted_at IS NULL
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;

SELECT '' AS '';
SELECT '=== [NO INDEX] 브랜드 필터 + 좋아요순 ===' AS '';
EXPLAIN SELECT id, name, price, brand_id, like_count, created_at
FROM products
WHERE brand_id = 1 AND deleted_at IS NULL
ORDER BY like_count DESC
LIMIT 20 OFFSET 0;

-- ================================================================
-- [3] 실행시간 측정 (인덱스 없는 상태)
-- ================================================================

SELECT '' AS '';
SELECT '=== [실행시간 - NO INDEX] 브랜드 필터 + 좋아요순 ===' AS '';
SET profiling = 1;
SELECT id, name, price, brand_id, like_count
FROM products
WHERE brand_id = 1 AND deleted_at IS NULL
ORDER BY like_count DESC
LIMIT 20;
SHOW PROFILES;

-- ================================================================
-- [4] 인덱스 복구
-- ================================================================

CREATE INDEX idx_products_created_at       ON products(created_at DESC);
CREATE INDEX idx_products_price            ON products(price ASC);
CREATE INDEX idx_products_like_count       ON products(like_count DESC);
CREATE INDEX idx_products_brand_created_at ON products(brand_id, created_at DESC);
CREATE INDEX idx_products_brand_price      ON products(brand_id, price ASC);
CREATE INDEX idx_products_brand_like_count ON products(brand_id, like_count DESC);

-- ================================================================
-- [5] 실행시간 측정 (인덱스 있는 상태)
-- ================================================================

SELECT '' AS '';
SELECT '=== [실행시간 - WITH INDEX] 브랜드 필터 + 좋아요순 ===' AS '';
SET profiling = 1;
SELECT id, name, price, brand_id, like_count
FROM products
WHERE brand_id = 1 AND deleted_at IS NULL
ORDER BY like_count DESC
LIMIT 20;
SHOW PROFILES;
