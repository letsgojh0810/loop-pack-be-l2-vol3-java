# 04. ERD (Entity-Relationship Diagram)

## 1. 전체 ERD

### 왜 이 다이어그램이 필요한가

ERD는 실제 데이터베이스에 어떤 테이블이 생기고, 테이블 간 관계가 어떻게 연결되는지를 보여준다.
도메인 모델(클래스 다이어그램)과 영속성 구조(ERD) 사이의 간극이 없는지 검증한다.

검증 포인트:
- 관계의 주인(FK 위치)이 올바른가
- 스냅샷 데이터가 원본과 분리되어 있는가
- 정규화 수준이 적절한가

### 다이어그램

```mermaid
erDiagram
    users ||--o{ product_likes : "좋아요"
    users ||--o{ orders : "주문"
    brands ||--o{ products : "소속 상품"
    products ||--o{ product_likes : "좋아요 대상"
    orders ||--o{ order_items : "주문 항목"

    users {
        bigint id PK "AUTO_INCREMENT"
        varchar(50) login_id UK "NOT NULL"
        varchar(255) password "NOT NULL"
        varchar(100) name "NOT NULL"
        date birth_date "NOT NULL"
        varchar(255) email "NOT NULL"
        datetime created_at "NOT NULL"
        datetime updated_at "NOT NULL"
        datetime deleted_at "NULL"
    }

    brands {
        bigint id PK "AUTO_INCREMENT"
        varchar(100) name UK "NOT NULL"
        varchar(500) description "NULL"
        varchar(500) image_url "NULL"
        datetime created_at "NOT NULL"
        datetime updated_at "NOT NULL"
        datetime deleted_at "NULL"
    }

    products {
        bigint id PK "AUTO_INCREMENT"
        bigint brand_id FK "NOT NULL → brands.id"
        varchar(200) name "NOT NULL"
        varchar(1000) description "NULL"
        int price "NOT NULL, >= 0"
        int stock "NOT NULL, >= 0"
        varchar(500) image_url "NULL"
        datetime created_at "NOT NULL"
        datetime updated_at "NOT NULL"
        datetime deleted_at "NULL"
    }

    product_likes {
        bigint id PK "AUTO_INCREMENT"
        bigint user_id FK "NOT NULL → users.id"
        bigint product_id FK "NOT NULL → products.id"
        datetime created_at "NOT NULL"
    }

    orders {
        bigint id PK "AUTO_INCREMENT"
        bigint user_id FK "NOT NULL → users.id"
        int total_amount "NOT NULL, >= 0"
        datetime created_at "NOT NULL"
        datetime updated_at "NOT NULL"
        datetime deleted_at "NULL"
    }

    order_items {
        bigint id PK "AUTO_INCREMENT"
        bigint order_id FK "NOT NULL → orders.id"
        bigint product_id "NOT NULL (참조용, FK 아님)"
        varchar(200) product_name "NOT NULL (스냅샷)"
        int product_price "NOT NULL (스냅샷)"
        varchar(100) brand_name "NOT NULL (스냅샷)"
        int quantity "NOT NULL, >= 1"
        datetime created_at "NOT NULL"
    }
```

### 이 구조에서 봐야 할 포인트

1. **order_items.product_id는 FK가 아니다.** 원본 상품이 삭제되어도 주문 항목은 남아야 하므로, 외래키 제약을 걸지 않는다. 대신 스냅샷 필드(product_name, product_price, brand_name)에 주문 시점의 데이터가 복사되어 있다.
2. **product_likes에는 `(user_id, product_id)` 유니크 제약**이 걸린다. 같은 유저가 같은 상품에 두 번 좋아요할 수 없다. 이 제약이 멱등성의 DB 레벨 안전장치다.
3. **soft delete 대상은 brands, products, orders**이다. `deleted_at` 컬럼이 NULL이면 활성, 값이 있으면 삭제된 상태다. product_likes와 order_items에는 soft delete가 없다.

---

## 2. 인덱스 설계

| 테이블 | 인덱스 | 컬럼 | 용도 |
|--------|--------|------|------|
| `products` | `idx_products_brand_id` | `brand_id` | 브랜드별 상품 필터링 |
| `products` | `idx_products_created_at` | `created_at DESC` | `latest` 정렬 |
| `products` | `idx_products_price` | `price ASC` | `price_asc` 정렬 |
| `product_likes` | `uk_product_likes_user_product` | `user_id, product_id` (UNIQUE) | 중복 좋아요 방지 + 존재 여부 조회 |
| `product_likes` | `idx_product_likes_product_id` | `product_id` | 상품별 좋아요 수 집계 |
| `product_likes` | `idx_product_likes_user_id` | `user_id` | 유저별 좋아요 목록 조회 |
| `orders` | `idx_orders_user_id_created_at` | `user_id, created_at` | 유저별 기간 주문 조회 |
| `order_items` | `idx_order_items_order_id` | `order_id` | 주문별 항목 조회 |

---

## 3. 테이블별 상세

### brands

```sql
CREATE TABLE brands (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(500),
    image_url   VARCHAR(500),
    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NOT NULL,
    deleted_at  DATETIME(6),
    UNIQUE KEY uk_brands_name (name)
);
```

### products

```sql
CREATE TABLE products (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id    BIGINT        NOT NULL,
    name        VARCHAR(200)  NOT NULL,
    description VARCHAR(1000),
    price       INT           NOT NULL,
    stock       INT           NOT NULL,
    image_url   VARCHAR(500),
    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NOT NULL,
    deleted_at  DATETIME(6),
    CONSTRAINT fk_products_brand_id FOREIGN KEY (brand_id) REFERENCES brands (id),
    INDEX idx_products_brand_id (brand_id),
    INDEX idx_products_created_at (created_at DESC)
);
```

### product_likes

```sql
CREATE TABLE product_likes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    product_id  BIGINT      NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    CONSTRAINT fk_product_likes_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_product_likes_product_id FOREIGN KEY (product_id) REFERENCES products (id),
    UNIQUE KEY uk_product_likes_user_product (user_id, product_id),
    INDEX idx_product_likes_product_id (product_id),
    INDEX idx_product_likes_user_id (user_id)
);
```

### orders

```sql
CREATE TABLE orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    total_amount INT         NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    deleted_at   DATETIME(6),
    CONSTRAINT fk_orders_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_orders_user_id_created_at (user_id, created_at)
);
```

### order_items

```sql
CREATE TABLE order_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT       NOT NULL,
    product_id    BIGINT       NOT NULL,
    product_name  VARCHAR(200) NOT NULL,
    product_price INT          NOT NULL,
    brand_name    VARCHAR(100) NOT NULL,
    quantity      INT          NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    CONSTRAINT fk_order_items_order_id FOREIGN KEY (order_id) REFERENCES orders (id),
    INDEX idx_order_items_order_id (order_id)
);
```

> **주의:** `order_items.product_id`에는 FK를 걸지 않는다. 상품이 삭제되어도 주문 항목은 보존되어야 하기 때문이다.

---

## 4. 데이터 정합성 전략

| 항목 | 전략 | 설명 |
|------|------|------|
| 좋아요 중복 방지 | UNIQUE 제약 | `(user_id, product_id)` 유니크 인덱스로 DB 레벨에서 보장 |
| 재고 음수 방지 | Application 레벨 검증 | `Product.decreaseStock()`에서 재고 부족 시 예외. 향후 `stock >= 0` CHECK 제약 또는 비관적 락 추가 가능. |
| 주문-상품 정합성 | 스냅샷 분리 | order_items에 상품 정보 복사. 원본 변경/삭제에 독립적. |
| 브랜드-상품 종속성 | Cascade Soft Delete | 브랜드 삭제 시 Application 레벨에서 소속 상품 일괄 soft delete |
| Soft Delete 필터링 | WHERE 조건 | 고객 API 조회 시 `WHERE deleted_at IS NULL` 조건 필수 |

---

## 잠재 리스크

| 리스크 | 영향 | 선택지 |
|--------|------|--------|
| soft delete 필터링 누락 | 삭제된 상품/브랜드가 고객에게 노출됨 | A) `@Where` 어노테이션 활용 B) Repository 메서드명에 규칙 적용 (`findActiveBy...`) |
| 동시 주문 시 재고 초과 차감 | 두 유저가 동시에 마지막 1개 주문 시 재고가 음수 | A) 비관적 락 (`SELECT FOR UPDATE`) B) 낙관적 락 (`@Version`) C) DB 레벨 `CHECK (stock >= 0)` |
| 좋아요 수 집계 성능 | `likes_desc` 정렬마다 COUNT 서브쿼리 | A) 현재는 COUNT로 시작 B) 트래픽 증가 시 Product에 `like_count` 컬럼 비정규화 |
| order_items에 FK 없는 product_id | 존재하지 않는 product_id 저장 가능 | Application 레벨에서 주문 생성 시 검증으로 충분. 조회 시에는 스냅샷 데이터만 사용. |
