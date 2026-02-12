# 02. 시퀀스 다이어그램

## 1. 주문 생성 흐름

### 왜 이 다이어그램이 필요한가

주문 생성은 이 시스템에서 가장 복잡한 흐름이다.
입력 검증 → 상품 존재/삭제 확인 → 재고 확인 → 차감 → 브랜드 조회 → 스냅샷 저장 → 주문 생성이 **하나의 트랜잭션** 안에서 일어나야 하고, 어느 단계에서 실패하든 전체가 롤백되어야 한다.

특히 검증해야 할 건:
- 요청 레벨 검증(중복 상품, 수량 범위)과 도메인 레벨 검증(상품 존재, 재고)의 **순서와 책임 분리**
- 재고 차감과 주문 저장이 묶이는 **트랜잭션 경계**
- 스냅샷에 필요한 브랜드 정보를 **어디서 가져오는지**

### 다이어그램

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as OrderV1Controller
    participant Facade as OrderFacade
    participant ProductService as ProductService
    participant BrandService as BrandService
    participant Product as Product
    participant Order as Order
    participant OrderRepo as OrderRepository

    Client->>Controller: POST /api/v1/orders<br/>{items: [{productId, quantity}]}

    Note over Controller: 요청 검증<br/>- 항목 비어있는지<br/>- 동일 상품 중복 여부<br/>- 수량 1~99 범위

    Controller->>Facade: createOrder(userId, command)

    Note over Facade: 트랜잭션 시작

    Facade->>ProductService: getActiveProducts(productIds)
    Note over ProductService: deleted_at IS NULL 필터링
    alt 상품 없음 or 삭제됨
        ProductService-->>Facade: NOT_FOUND
        Facade--xClient: 404
    end
    ProductService-->>Facade: List<Product>

    loop 각 주문 항목에 대해
        Facade->>Product: hasEnoughStock(quantity)
        alt 재고 부족
            Product-->>Facade: false
            Facade--xClient: 400 Bad Request (재고 부족)
        end
        Facade->>Product: decreaseStock(quantity)
    end

    Facade->>BrandService: getBrands(brandIds)
    BrandService-->>Facade: List<Brand>

    Facade->>Order: create(userId, products, brands, quantities)
    Note over Order: 스냅샷 저장<br/>상품명, 가격, 브랜드명 → OrderItem
    Note over Order: 총 금액 계산<br/>sum(price × quantity)

    Facade->>OrderRepo: save(order)

    Note over Facade: 트랜잭션 커밋

    Facade-->>Controller: OrderInfo
    Controller-->>Client: 200 OK + 주문 정보
```

### 이 구조에서 봐야 할 포인트

1. **검증이 두 단계로 나뉜다.** 요청 형식 검증(중복 상품, 수량 범위)은 Controller에서, 도메인 검증(상품 존재, 재고 충분)은 트랜잭션 안에서 한다. 형식이 잘못된 요청은 트랜잭션을 열기도 전에 걸러낸다.
2. **ProductService.getActiveProducts()가 삭제된 상품을 필터링**한다. 메서드 이름에 "Active"를 넣어서 soft delete 필터링이 적용된다는 걸 명시적으로 드러낸다. 요청한 productId 중 하나라도 조회되지 않으면 404.
3. **BrandService 호출이 재고 차감 이후에 있다.** 재고가 부족하면 브랜드를 조회할 필요 자체가 없으므로, 불필요한 쿼리를 아끼기 위해 이 순서로 배치했다.
4. **트랜잭션 하나에 모든 게 묶인다.** 상품이 10개, 20개여도 현재는 하나의 트랜잭션이다. 상품 수가 극단적으로 많아지면 락 시간이 길어지는 리스크가 있지만, 현재 규모에서는 정합성이 더 중요하다.

---

## 2. 좋아요 등록/취소 흐름

### 왜 이 다이어그램이 필요한가

좋아요의 핵심은 **멱등성**이다. 같은 요청이 여러 번 와도 결과가 동일해야 하며, 등록과 취소에서 상품 검증 범위가 다르다는 점이 설계 의도를 명확히 드러내야 할 부분이다.

### 다이어그램

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as ProductLikeV1Controller
    participant Facade as ProductLikeFacade
    participant ProductService as ProductService
    participant LikeService as ProductLikeService
    participant LikeRepo as ProductLikeRepository

    rect rgb(230, 245, 230)
        Note right of Client: 좋아요 등록
        Client->>Controller: POST /api/v1/products/{productId}/likes
        Controller->>Facade: like(userId, productId)
        Facade->>ProductService: getActiveProduct(productId)
        alt 상품 없음 or 삭제됨
            ProductService-->>Facade: NOT_FOUND
            Facade--xClient: 404
        end
        Facade->>LikeService: like(userId, productId)
        LikeService->>LikeRepo: findByUserIdAndProductId()
        alt 이미 좋아요 상태
            LikeRepo-->>LikeService: 존재함
            LikeService-->>Facade: (무시, 멱등 처리)
        else 좋아요 없음
            LikeRepo-->>LikeService: 없음
            LikeService->>LikeRepo: save(new ProductLike)
        end
        Facade-->>Controller: OK
        Controller-->>Client: 200 OK
    end

    rect rgb(245, 230, 230)
        Note right of Client: 좋아요 취소
        Client->>Controller: DELETE /api/v1/products/{productId}/likes
        Controller->>Facade: unlike(userId, productId)
        Note over Facade: 상품 존재 검증 생략
        Facade->>LikeService: unlike(userId, productId)
        LikeService->>LikeRepo: findByUserIdAndProductId()
        alt 좋아요 존재
            LikeRepo-->>LikeService: 존재함
            LikeService->>LikeRepo: delete(productLike)
        else 좋아요 없음
            LikeRepo-->>LikeService: 없음
            LikeService-->>Facade: (무시, 멱등 처리)
        end
        Facade-->>Controller: OK
        Controller-->>Client: 200 OK
    end
```

### 이 구조에서 봐야 할 포인트

1. **등록 시에만 상품 존재 여부를 확인하고, 취소 시에는 생략한다.** 이유는 명확하다 — 상품이 삭제된 후에도 유저가 기존 좋아요를 해제할 수 있어야 한다. 삭제된 상품의 좋아요를 취소하려는데 "상품이 없습니다"라고 하면 유저 입장에서 답답하다.
2. **멱등 처리는 LikeService에서 판단한다.** 이미 좋아요가 있으면 등록을 무시하고, 없으면 삭제를 무시한다. 에러를 던지지 않으므로 클라이언트는 현재 상태를 몰라도 안전하게 요청할 수 있다.
3. **좋아요는 물리 삭제(hard delete)를 사용한다.** 좋아요 이력을 보존할 필요가 없고, 토글 성격이므로 soft delete는 과하다. `(user_id, product_id)` 유니크 제약이 있으므로 soft delete를 쓰면 재등록 시 충돌 문제도 생긴다.

---

## 3. 브랜드 삭제 (어드민) 흐름

### 왜 이 다이어그램이 필요한가

브랜드 삭제는 **cascade soft delete**가 발생하는 유일한 흐름이다. 브랜드 하나를 삭제하면 소속 상품 전체가 함께 soft delete 되므로 영향 범위가 크다. 기존 주문이나 좋아요 데이터에 영향이 없는지를 검증해야 한다.

### 다이어그램

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant Controller as BrandAdminV1Controller
    participant Facade as BrandFacade
    participant BrandService as BrandService
    participant ProductService as ProductService
    participant Brand as Brand
    participant Product as Product

    Admin->>Controller: DELETE /api-admin/v1/brands/{brandId}
    Controller->>Facade: deleteBrand(brandId)

    Note over Facade: 트랜잭션 시작

    Facade->>BrandService: getActiveBrand(brandId)
    alt 브랜드 없음 or 이미 삭제됨
        BrandService-->>Facade: NOT_FOUND
        Facade--xAdmin: 404
    end

    Facade->>Brand: delete()
    Note over Brand: deletedAt = now()

    Facade->>ProductService: getActiveProductsByBrandId(brandId)
    loop 해당 브랜드의 각 활성 상품
        Facade->>Product: delete()
        Note over Product: deletedAt = now()
    end

    Note over Facade: 트랜잭션 커밋

    Facade-->>Controller: OK
    Controller-->>Admin: 200 OK

    Note over Admin: 기존 주문의 OrderItem 스냅샷은<br/>영향 없음 (이미 복사된 데이터)
    Note over Admin: 기존 좋아요 레코드는 유지됨<br/>고객 목록 조회 시 쿼리 레벨에서 필터링
```

### 이 구조에서 봐야 할 포인트

1. **브랜드와 소속 상품이 하나의 트랜잭션으로 처리된다.** 상품 삭제 도중 실패하면 브랜드 삭제도 롤백된다. 원자성이 보장된다.
2. **주문 데이터는 안전하다.** OrderItem에 상품명, 가격, 브랜드명이 스냅샷으로 복사되어 있으므로, 원본이 삭제되어도 주문 이력 조회에 문제가 없다. 이게 스냅샷을 도입한 핵심 이유다.
3. **좋아요 레코드 자체는 삭제하지 않는다.** cascade 범위를 좋아요까지 넓히면 트랜잭션이 더 비대해진다. 대신 고객이 좋아요 목록을 조회할 때 `products.deleted_at IS NULL` join으로 삭제된 상품을 걸러낸다.

---

## 잠재 리스크

| 리스크 | 영향 | 대안 |
|--------|------|------|
| 주문 생성 트랜잭션이 비대해질 수 있음 | 상품 수가 많으면 재고 차감마다 row lock, 트랜잭션 시간 증가 | 향후 비관적 락 도입 시 락 순서를 productId 순으로 고정하여 데드락 방지 |
| 브랜드 삭제 시 상품이 수천 개면 느릴 수 있음 | 트랜잭션 시간 증가, 타임아웃 가능 | 배치 처리 또는 `UPDATE products SET deleted_at = now() WHERE brand_id = ?` 벌크 쿼리로 전환 |
| 좋아요 COUNT 쿼리가 상품 목록 정렬에 사용됨 | `likes_desc` 정렬 시 매번 서브쿼리 집계 필요 | 트래픽 증가 시 Product에 `like_count` 비정규화 필드 도입 |
| 좋아요 목록에서 삭제된 상품 필터링 | join 조건으로 처리하므로 쿼리 복잡도 약간 증가 | 현재 규모에서는 문제 없음. 데이터 증가 시 인덱스 튜닝으로 대응 |
