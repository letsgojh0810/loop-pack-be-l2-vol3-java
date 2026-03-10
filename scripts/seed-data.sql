-- ============================================================
-- 10만건 성능 테스트용 Seed Data
-- 실행: mysql -h 127.0.0.1 -u application -papplication loopers < scripts/seed-data.sql
-- ============================================================

USE loopers;

-- 기존 테스트 데이터 정리 (선택적)
-- DELETE FROM product_likes;
-- DELETE FROM order_items;
-- DELETE FROM orders;
-- DELETE FROM products;
-- DELETE FROM brands;

-- ============================================================
-- 브랜드 10개 INSERT
-- ============================================================
INSERT INTO brands (name, description, image_url, created_at, updated_at)
VALUES
  ('Nike', '나이키 - Just Do It', 'https://example.com/nike.jpg', NOW(), NOW()),
  ('Adidas', '아디다스 - Impossible is Nothing', 'https://example.com/adidas.jpg', NOW(), NOW()),
  ('Puma', '푸마 - Forever Faster', 'https://example.com/puma.jpg', NOW(), NOW()),
  ('New Balance', '뉴발란스 - Fearlessly Independent', 'https://example.com/nb.jpg', NOW(), NOW()),
  ('Reebok', '리복 - Be More Human', 'https://example.com/reebok.jpg', NOW(), NOW()),
  ('Under Armour', '언더아머 - I Will', 'https://example.com/ua.jpg', NOW(), NOW()),
  ('Converse', '컨버스 - Shoes Are Boring. Wear Sneakers.', 'https://example.com/converse.jpg', NOW(), NOW()),
  ('Vans', '반스 - Off the Wall', 'https://example.com/vans.jpg', NOW(), NOW()),
  ('Asics', '아식스 - Sound Mind Sound Body', 'https://example.com/asics.jpg', NOW(), NOW()),
  ('Saucony', '사코니 - Run For Good', 'https://example.com/saucony.jpg', NOW(), NOW());

-- ============================================================
-- 상품 10만건 INSERT (프로시저 사용)
-- ============================================================
DROP PROCEDURE IF EXISTS seed_products;

DELIMITER $$
CREATE PROCEDURE seed_products()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE brand_id INT;
  DECLARE like_cnt INT;

  WHILE i <= 100000 DO
    SET brand_id = (i % 10) + 1;
    SET like_cnt = FLOOR(RAND() * 1000);

    INSERT INTO products (brand_id, name, description, price, stock, image_url, like_count, created_at, updated_at)
    VALUES (
      brand_id,
      CONCAT('상품_', LPAD(i, 6, '0')),
      CONCAT('상품 ', i, '번의 상세 설명입니다.'),
      FLOOR(RAND() * 990000 + 10000),  -- 10,000 ~ 1,000,000원
      FLOOR(RAND() * 1000),             -- 0 ~ 999개
      CONCAT('https://example.com/product/', i, '.jpg'),
      like_cnt,
      DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),
      NOW()
    );

    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;

CALL seed_products();
DROP PROCEDURE IF EXISTS seed_products;

SELECT COUNT(*) AS total_products FROM products;
SELECT COUNT(*) AS total_brands FROM brands;
