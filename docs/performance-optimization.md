# 상품 조회 성능 개선 - 인덱스, 비정규화, Redis 캐시

> 작성일: 2026-03-11
> 브랜치: `feat/performance-optimization`
> 스택: Spring Boot 3.4.4 · JPA · Redis · k6 · Grafana · InfluxDB

---

## 목차

1. [과제 요구사항](#1-과제-요구사항)
2. [문제 분석](#2-문제-분석)
3. [개발 내용](#3-개발-내용)
4. [EXPLAIN 분석 - 인덱스 전후 비교](#4-explain-분석---인덱스-전후-비교)
5. [트러블슈팅](#5-트러블슈팅)
6. [부하 테스트 환경 구성](#6-부하-테스트-환경-구성)
7. [테스트 결과](#7-테스트-결과)
8. [결론 및 회고](#8-결론-및-회고)

---

## 1. 과제 요구사항

- 상품 목록 조회 API에 **페이지네이션** 및 **정렬** 기능 추가
  - 정렬 기준: `latest` (최신순), `price_asc` (가격 낮은순), `likes_desc` (좋아요 많은순)
  - 페이지네이션: `page`, `size` 파라미터
- **Redis 캐시** 적용으로 DB 부하 감소
  - 상품 상세: TTL 10분
  - 상품 목록: TTL 5분
- **10만 건** 데이터 기준 부하 테스트 수행
- k6 + Grafana로 성능 지표 시각화

---

## 2. 문제 분석

### 2.1 기존 코드의 문제점

#### 상품 목록 API - 페이지네이션 없음

기존 `ProductV1Controller`는 `brandId`만 파라미터로 받고 전체 데이터를 조회했다.

```java
// 기존 (문제)
@GetMapping
public ApiResponse<ProductV1Dto.ProductListResponse> getProducts(
    @RequestParam(required = false) Long brandId
) {
    List<ProductInfo> infos = productFacade.getProducts(brandId);
    return ApiResponse.success(ProductV1Dto.ProductListResponse.from(infos));
}
```

10만 건 전체 조회 시 응답 시간 약 **26,472ms**, 오류율 **97.4%** (타임아웃).

#### Brand N+1 문제

상품 목록 조회 시 각 상품마다 브랜드를 개별 조회하고 있었다.

```java
// 기존 (N+1 문제)
for (Product product : products) {
    Brand brand = brandService.getBrand(product.getBrandId()); // 상품 수만큼 쿼리 발생
    result.add(ProductInfo.of(product, brand, ...));
}
```

상품 20개를 조회하면 Brand 쿼리가 최대 20번 추가 실행된다.

---

## 3. 개발 내용

### 3.1 ProductSort 열거형 추가

정렬 기준을 도메인 레이어에서 타입 안전하게 관리하기 위해 열거형을 신설했다.

**파일**: `domain/product/ProductSort.java`

```java
public enum ProductSort {
    LATEST, PRICE_ASC, LIKES_DESC;

    public static ProductSort from(String value) {
        return switch (value.toLowerCase()) {
            case "latest"     -> LATEST;
            case "price_asc"  -> PRICE_ASC;
            case "likes_desc" -> LIKES_DESC;
            default -> throw new CoreException(ErrorType.BAD_REQUEST,
                "유효하지 않은 정렬 기준입니다: " + value);
        };
    }
}
```

**설계 의도**: Controller에서 문자열로 받은 정렬 값을 도메인 레이어 진입 시점에 변환한다. 이후 레이어는 `String` 대신 `ProductSort`를 사용하므로 오타나 잘못된 값이 내부로 전파되지 않는다.

---

### 3.2 상품 목록 페이지네이션 구현

**Repository 레이어**: JPA `Pageable` 활용

```java
// ProductJpaRepository.java
Page<Product> findAllByDeletedAtIsNull(Pageable pageable);
Page<Product> findAllByBrandIdAndDeletedAtIsNull(Long brandId, Pageable pageable);
```

**Infrastructure 레이어**: `ProductSort` → `Sort` 변환

```java
// ProductRepositoryImpl.java
@Override
public List<Product> findAllPaged(Long brandId, ProductSort sort, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, toSort(sort));
    if (brandId != null) {
        return productJpaRepository
            .findAllByBrandIdAndDeletedAtIsNull(brandId, pageable).getContent();
    }
    return productJpaRepository
        .findAllByDeletedAtIsNull(pageable).getContent();
}

private Sort toSort(ProductSort sort) {
    return switch (sort) {
        case LATEST     -> Sort.by("createdAt").descending();
        case PRICE_ASC  -> Sort.by("price").ascending();
        case LIKES_DESC -> Sort.by("likeCount").descending();
    };
}
```

**Controller 레이어**: `sort`, `page`, `size` 파라미터 추가

```java
// ProductV1Controller.java
@GetMapping
public ApiResponse<ProductV1Dto.ProductListResponse> getProducts(
    @RequestParam(required = false) Long brandId,
    @RequestParam(defaultValue = "latest") String sort,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    ProductSort productSort = ProductSort.from(sort);
    List<ProductInfo> infos = productFacade.getProducts(brandId, productSort, page, size);
    return ApiResponse.success(ProductV1Dto.ProductListResponse.from(infos));
}
```

---

### 3.3 Brand N+1 문제 해결

브랜드를 한 번의 IN 쿼리로 일괄 조회하도록 변경했다.

```java
// BrandService.java
public Map<Long, Brand> getBrandsByIds(List<Long> ids) {
    return brandRepository.findAllByIds(ids).stream()
        .collect(Collectors.toMap(Brand::getId, b -> b));
}
```

```java
// ProductFacade.java - 변경 후
public List<ProductInfo> getProducts(Long brandId, ProductSort sort, int page, int size) {
    List<Product> products = productService.getProducts(brandId, sort, page, size);

    List<Long> brandIds = products.stream()
        .map(Product::getBrandId).distinct().toList();
    Map<Long, Brand> brandMap = brandService.getBrandsByIds(brandIds); // 쿼리 1번

    List<ProductInfo> result = new ArrayList<>();
    for (Product product : products) {
        result.add(ProductInfo.of(
            product, brandMap.get(product.getBrandId()),
            product.getLikeCount(), false));
    }
    return result;
}
```

**개선 효과**: 상품 N개 조회 시 Brand 쿼리가 N번 → 1번으로 감소.

---

### 3.4 Redis 캐시 적용

#### CacheConfig 설정

```java
@EnableCaching
@Configuration
public class CacheConfig {

    public static final String PRODUCT_DETAIL = "productDetail";
    public static final String PRODUCT_LIST   = "productList";

    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory lettuceConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new ParameterNamesModule()); // record 역직렬화용
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.EVERYTHING, // record(final 클래스) 포함
            JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(...)
            .serializeValuesWith(...)
            .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
            PRODUCT_DETAIL, defaultConfig.entryTtl(Duration.ofMinutes(10)),
            PRODUCT_LIST,   defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        return RedisCacheManager.builder(lettuceConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
```

#### Facade 캐시 어노테이션

```java
// 상품 상세 - productId 키로 캐싱
@Cacheable(cacheNames = CacheConfig.PRODUCT_DETAIL, key = "#productId")
public ProductInfo getProductDetail(Long productId, Long userId) { ... }

// 상품 목록 - 조합 키로 캐싱
@Cacheable(
    cacheNames = CacheConfig.PRODUCT_LIST,
    key = "(#brandId ?: 'all') + '_' + #sort + '_' + #page + '_' + #size"
)
public List<ProductInfo> getProducts(Long brandId, ProductSort sort, int page, int size) { ... }

// 상품 수정/삭제 시 캐시 무효화
@Caching(evict = {
    @CacheEvict(cacheNames = CacheConfig.PRODUCT_DETAIL, key = "#productId"),
    @CacheEvict(cacheNames = CacheConfig.PRODUCT_LIST, allEntries = true)
})
public ProductInfo updateProduct(...) { ... }
```

---

## 4. EXPLAIN 분석 - 인덱스 전후 비교

> 환경: MySQL 8.0 · 상품 10만 건 · 브랜드 10개

### 4.1 인덱스 설계

상품 목록 조회의 쿼리 패턴은 크게 두 가지다.

- **전체 조회**: `WHERE deleted_at IS NULL ORDER BY {정렬컬럼} LIMIT 20`
- **브랜드 필터 조회**: `WHERE brand_id = ? AND deleted_at IS NULL ORDER BY {정렬컬럼} LIMIT 20`

정렬 기준이 3가지(`latest`, `price_asc`, `likes_desc`)이므로 총 6개의 쿼리 패턴이 존재한다.

```sql
-- 적용한 인덱스
-- 전체 조회용 (정렬만)
CREATE INDEX idx_products_created_at  ON products(created_at DESC);
CREATE INDEX idx_products_price       ON products(price ASC);
CREATE INDEX idx_products_like_count  ON products(like_count DESC);

-- 브랜드 필터 + 정렬 복합 인덱스
-- brand_id를 선행 컬럼으로 두면 range scan 후 정렬 인덱스를 연속 사용 가능
CREATE INDEX idx_products_brand_created_at ON products(brand_id, created_at DESC);
CREATE INDEX idx_products_brand_price      ON products(brand_id, price ASC);
CREATE INDEX idx_products_brand_like_count ON products(brand_id, like_count DESC);
```

### 4.2 BEFORE - 인덱스 없는 상태 (`idx_products_brand_id_like_count` 단 하나)

```
EXPLAIN SELECT ... FROM products WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT 20;
```

| 쿼리 패턴 | type | key | rows | Extra |
|----------|------|-----|------|-------|
| 전체 + latest | **ALL** | NULL | **93,780** | Using where; **Using filesort** |
| 전체 + price_asc | **ALL** | NULL | **93,780** | Using where; **Using filesort** |
| 전체 + likes_desc | **ALL** | NULL | **93,780** | Using where; **Using filesort** |
| 브랜드 + latest | ref | idx_products_brand_id_like_count | 18,614 | Using where; **Using filesort** |
| 브랜드 + price_asc | ref | idx_products_brand_id_like_count | 18,614 | Using where; **Using filesort** |
| 브랜드 + likes_desc | ref | idx_products_brand_id_like_count | 18,614 | Using where |

**핵심 문제**: 전체 조회 3가지가 Full Table Scan(`ALL`). 10만 건을 전부 읽은 뒤 메모리에서 정렬(`filesort`).

### 4.3 AFTER - 6개 인덱스 적용 후

| 쿼리 패턴 | type | key | rows | Extra |
|----------|------|-----|------|-------|
| 전체 + latest | **index** | idx_products_created_at | **20** | Using where |
| 전체 + price_asc | **index** | idx_products_price | **20** | Using where |
| 전체 + likes_desc | **index** | idx_products_like_count | **20** | Using where |
| 브랜드 + latest | ref | idx_products_brand_created_at | 18,614 | Using where |
| 브랜드 + price_asc | ref | idx_products_brand_price | 18,614 | Using where |
| 브랜드 + likes_desc | ref | idx_products_brand_like_count | 18,614 | Using where |

**개선 포인트**:
- `ALL` → `index`: 전체 조회 시 인덱스만 스캔하여 정렬 없이 20건만 읽음
- `Using filesort` 완전 제거: 인덱스 순서가 곧 정렬 순서
- 브랜드 필터 쿼리에서 기존 `idx_products_brand_id_like_count` 외 나머지 2개도 최적 인덱스 선택

### 4.4 실행 시간 측정 (10만 건 기준)

| 쿼리 | BEFORE | AFTER | 개선율 |
|------|--------|-------|--------|
| 전체 최신순 | **23.2ms** | **3.6ms** | **6.5배** |
| 전체 좋아요순 | **16.8ms** | **0.4ms** | **44배** |

> 측정: MySQL `SET profiling = 1; SHOW PROFILES;`

### 4.5 인덱스 설계 트레이드오프

**장점**:
- 읽기 성능 대폭 향상 (filesort 제거, 읽는 rows 수 감소)
- 복합 인덱스(brand_id + 정렬)가 WHERE + ORDER BY를 동시에 커버

**단점**:
- 인덱스 6개 → 쓰기(INSERT/UPDATE) 시 인덱스 갱신 오버헤드 증가
- `like_count`는 좋아요 등록/취소마다 변경 → `idx_products_like_count`, `idx_products_brand_like_count` 갱신 비용
- 디스크 공간 추가 사용

**결론**: 상품 조회는 읽기 heavy, 쓰기 light 워크로드이므로 읽기 최적화 우선이 적절하다. 좋아요 수 변경은 비정규화된 `like_count` 컬럼을 트랜잭션 내에서 atomic하게 업데이트하므로 정합성이 보장된다.

---

## 5. 트러블슈팅

### 5.1 k6가 포트 8081에 요청 → 404

**증상**: k6 실행 시 모든 요청이 404 반환.

**원인**: k6 스크립트의 `BASE_URL`이 `localhost:8081`로 설정되어 있었는데, 8081은 `management.server.port`(Actuator 전용)였다. 실제 API 서버는 8080.

```yaml
# monitoring.yml
management:
  server:
    port: 8081
```

**해결**: k6 스크립트 BASE_URL을 `localhost:8080`으로 수정.

---

### 5.2 앱 재시작 시 DB 초기화 (ddl-auto: create)

**증상**: Spring 앱을 재시작할 때마다 10만 건 데이터가 사라짐.

**원인**: 로컬 프로파일의 `spring.jpa.hibernate.ddl-auto: create` 설정이 앱 시작 시 모든 테이블을 DROP → CREATE.

**해결**: 앱 재시작 후 항상 시드 데이터를 다시 삽입하는 워크플로우 수립.

```bash
docker exec -i docker-mysql-1 mysql -u application -papplication loopers < scripts/seed-data.sql
```

---

### 5.3 Redis 캐시 히트 시 500 에러 - 원인 1: 이전 포맷 데이터

**증상**: `CacheConfig` 수정 후 첫 캐시 히트에서 500 에러 발생.

**원인**: Redis에 이전 직렬화 포맷으로 저장된 데이터가 새로운 역직렬화 로직과 충돌.

**해결**: Redis 전체 플러시.

```bash
docker exec redis-master redis-cli FLUSHALL
```

---

### 5.4 Redis 캐시 히트 시 500 에러 - 원인 2: `.toList()` 반환 타입

**증상**: `getProducts()`의 캐시 히트에서 `SerializationException` 발생.

**원인**: Java Stream의 `.toList()`는 `ImmutableCollections$ListN`을 반환한다. 이 클래스는 package-private이라 Jackson이 역직렬화 시 인스턴스를 생성할 수 없다.

```java
// 문제 코드
return products.stream()
    .map(...)
    .toList(); // → ImmutableCollections$ListN (Jackson 역직렬화 불가)
```

**해결**: 명시적으로 `ArrayList`를 생성해 반환.

```java
// 수정 코드
List<ProductInfo> result = new ArrayList<>();
for (Product product : products) {
    result.add(ProductInfo.of(...));
}
return result; // → ArrayList (Jackson 역직렬화 가능)
```

**왜 ArrayList는 되나?**: Jackson의 `defaultTyping`이 `NON_FINAL`일 때 `ArrayList`(non-final 클래스)에는 `@class` 타입 정보가 JSON에 포함된다. 따라서 역직렬화 시 `ArrayList`로 정확히 복원된다.

```json
// Redis에 저장된 형태 (ArrayList)
["java.util.ArrayList", [{"productId": 1, ...}, ...]]
```

---

### 5.5 Redis 캐시 히트 시 500 에러 - 원인 3: Java record의 `final` 특성

**증상**: `getProductDetail()` 캐시 히트에서 500 에러. `getProducts()`는 정상.

**원인**: `ProductInfo`가 Java `record` 타입이다. Java record는 암묵적으로 `final`이다. `GenericJackson2JsonRedisSerializer`에 설정한 `ObjectMapper.DefaultTyping.NON_FINAL`은 `final` 클래스에 `@class` 타입 정보를 추가하지 않는다.

따라서 Redis에 저장 시:

```json
// NON_FINAL - @class 없음 (문제)
{"productId": 1, "productName": "상품_000001", ...}
```

역직렬화 시 Jackson이 타입을 알 수 없어 `LinkedHashMap`으로 생성 → `(ProductInfo) linkedHashMap` 캐스팅 실패 → `ClassCastException` → 500.

**해결 시도 1**: `ParameterNamesModule` 추가 → 실패 (근본 원인이 아님)

```java
objectMapper.registerModule(new ParameterNamesModule());
// → 여전히 @class 없이 저장되어 LinkedHashMap으로 역직렬화됨
```

**해결 시도 2**: `DefaultTyping.NON_FINAL` → `DefaultTyping.EVERYTHING` 변경 → 성공

```java
objectMapper.activateDefaultTyping(
    LaissezFaireSubTypeValidator.instance,
    ObjectMapper.DefaultTyping.EVERYTHING, // final 클래스(record)에도 @class 추가
    JsonTypeInfo.As.PROPERTY
);
```

이제 Redis에 저장 형태:

```json
// EVERYTHING - @class 포함 (정상)
{
  "@class": "com.loopers.application.product.ProductInfo",
  "productId": 1,
  "productName": "상품_000001",
  ...
}
```

**`EVERYTHING` vs `NON_FINAL`**: `EVERYTHING`은 `String`, `int`, `boolean` 같은 기본 타입은 여전히 제외하고, record/final 클래스를 포함한 모든 객체 타입에 `@class`를 추가한다. 성능/용량 측면에서 약간 더 크지만 정확성이 보장된다.

**`ParameterNamesModule`은 왜 필요한가?**: `@class`로 타입을 알아도 record를 인스턴스화하려면 canonical constructor의 파라미터 이름이 필요하다. Spring Boot 3.x는 `-parameters` 컴파일 옵션을 기본 활성화하지만, 직접 생성한 `ObjectMapper`는 이 모듈을 자동 등록하지 않는다. 따라서 `ParameterNamesModule`을 명시적으로 등록해야 record 생성자 파라미터를 매핑할 수 있다.

---

## 6. 부하 테스트 환경 구성

### 6.1 인프라 구성

```
k6 → InfluxDB 1.8 → Grafana
```

```yaml
# docker/k6-compose.yml
services:
  influxdb:
    image: influxdb:1.8
    ports:
      - "8086:8086"
    environment:
      - INFLUXDB_DB=k6

  grafana-k6:
    image: grafana/grafana
    ports:
      - "3000:3000"
    volumes:
      - ./grafana/provisioning:/etc/grafana/provisioning
```

Grafana Datasource로 InfluxDB를 프로비저닝하고, k6 공식 대시보드(ID: 2587)로 시각화.

### 6.2 시드 데이터 생성

MySQL Stored Procedure로 10만 건 데이터를 생성했다.

```sql
-- scripts/seed-data.sql (요약)
DELIMITER //
CREATE PROCEDURE generate_data()
BEGIN
  -- 10개 브랜드 삽입
  INSERT INTO brands (name, ...) VALUES ('Nike', ...), ('Adidas', ...), ...;

  -- 10만 개 상품 삽입 (루프)
  SET i = 1;
  WHILE i <= 100000 DO
    INSERT INTO products (name, brand_id, price, stock, like_count, ...)
    VALUES (CONCAT('상품_', LPAD(i, 6, '0')),
            MOD(i, 10) + 1,
            FLOOR(RAND() * 990000) + 10000,
            FLOOR(RAND() * 1000),
            FLOOR(RAND() * 1000), ...);
    SET i = i + 1;
  END WHILE;
END//
CALL generate_data();
```

삽입 소요 시간: 약 **2분** (Docker MySQL 기준).

### 6.3 k6 테스트 시나리오

#### 상품 목록 조회 (`product-list.js`)

| 단계 | 시간 | VUs |
|------|------|-----|
| 워밍업 | 30초 | 0 → 10 |
| 부하 증가 | 1분 | 10 → 50 |
| 부하 유지 | 2분 | 50 |
| 쿨다운 | 30초 | 50 → 0 |

요청 패턴:
- 정렬: `latest` / `price_asc` / `likes_desc` 랜덤
- brandId: 50% 확률로 1~10 랜덤, 나머지는 전체 조회
- page: 0~49 랜덤

임계값: `p(95) < 500ms`, `error rate < 1%`

#### 상품 상세 조회 (`product-detail.js`)

| 단계 | 시간 | VUs |
|------|------|-----|
| 워밍업 | 30초 | 0 → 10 |
| 부하 증가 | 1분 | 10 → 100 |
| 부하 유지 | 2분 | 100 |
| 쿨다운 | 30초 | 100 → 0 |

요청 패턴:
- 70% - hot product IDs (1, 2, 3, 4, 5, 10, 100, 1000) → 캐시 히트 유도
- 30% - 1~100,000 랜덤 → 캐시 미스

임계값: `p(95) < 300ms`, `error rate < 1%`

---

## 7. 테스트 결과

### 7.1 캐시 적용 전후 비교 (단건 측정)

| API | 캐시 미스 (DB 조회) | 캐시 히트 (Redis) | 개선율 |
|-----|-------------------|------------------|--------|
| 상품 상세 | 3,067ms | 57ms | **약 54배** |
| 상품 목록 | 1,831ms | 234ms | **약 8배** |

### 7.2 k6 부하 테스트 결과

#### 상품 목록 API (50 VUs, 4분)

| 지표 | 결과 | 임계값 | 통과 |
|------|------|--------|------|
| 총 요청 수 | 16,920 | - | - |
| RPS | 70.4 req/s | - | - |
| 오류율 | 0.000% | < 1% | ✓ |
| 평균 응답시간 | 11.9ms | - | - |
| p90 응답시간 | 26.5ms | - | - |
| p95 응답시간 | 50.2ms | < 500ms | ✓ |
| 최대 응답시간 | 652.1ms | - | - |
| 체크 통과율 | 100% | - | - |

#### 상품 상세 API (100 VUs, 4분)

| 지표 | 결과 | 임계값 | 통과 |
|------|------|--------|------|
| 총 요청 수 | 54,660 | - | - |
| RPS | 227.7 req/s | - | - |
| 오류율 | 0.000% | < 1% | ✓ |
| 평균 응답시간 | 9.2ms | - | - |
| p90 응답시간 | 14.7ms | - | - |
| p95 응답시간 | 25.3ms | < 300ms | ✓ |
| 최대 응답시간 | 940.8ms | - | - |
| 체크 통과율 | 100% | - | - |

### 7.3 해석

**상품 상세**는 70% hot ID 전략으로 캐시 히트율이 높아 평균 9.2ms라는 매우 낮은 응답 시간을 달성했다. 100 VUs 동시 요청에도 p95 25ms를 유지.

**상품 목록**은 random page/sort/brandId 조합으로 캐시 히트율이 낮음에도 평균 11.9ms. DB 인덱스(`createdAt`, `price`, `likeCount`)와 페이지네이션 덕분에 전체 스캔 없이 효율적 조회.

---

## 8. 개발 환경 설정

### 8.1 MySQL MCP 서버 설정

Claude Code에서 `docker exec` 없이 직접 MySQL 쿼리를 실행할 수 있도록 MCP(Model Context Protocol) 서버를 설정했다. EXPLAIN 분석 및 데이터 확인 작업이 훨씬 간편해진다.

**설치**

```bash
npm install -g @benborla29/mcp-server-mysql
```

**Claude Code MCP 등록**

```bash
claude mcp add mcp_server_mysql \
  -e MYSQL_HOST="127.0.0.1" \
  -e MYSQL_PORT="3306" \
  -e MYSQL_USER="application" \
  -e MYSQL_PASS="application" \
  -e MYSQL_DB="loopers" \
  -e ALLOW_INSERT_OPERATION="true" \
  -e ALLOW_UPDATE_OPERATION="true" \
  -e ALLOW_DELETE_OPERATION="true" \
  -- npx @benborla29/mcp-server-mysql
```

등록 후 Claude Code를 재시작하면 자연어로 MySQL 쿼리 실행이 가능해진다. Docker가 실행 중인 상태에서만 MCP 서버가 연결된다.

---

## 9. 결론 및 회고

### 개선 포인트 정리

| 문제 | 해결책 | 효과 |
|------|--------|------|
| 페이지네이션 없음 → 전체 조회 | `Pageable` 기반 페이지네이션 | 10만 건 → 20건 조회로 DB 부하 감소 |
| Brand N+1 쿼리 | `getBrandsByIds()` 배치 조회 | N번 쿼리 → 1번 |
| 인덱스 1개 → 쿼리 패턴 일부 미커버 | 6개 인덱스로 확장 | filesort 제거, 좋아요순 44배 / 최신순 6.5배 개선 |
| 반복 DB 조회 | Redis 캐시 (TTL 5~10분) | 캐시 히트 시 8~54배 응답 속도 향상 |

### 배운 점

**1. Java record는 `final`이다**
`DefaultTyping.NON_FINAL`은 record에 `@class` 타입 정보를 추가하지 않는다. `GenericJackson2JsonRedisSerializer`와 record를 함께 사용할 때는 `EVERYTHING`을 사용해야 한다.

**2. `.toList()` vs `new ArrayList<>()`**
Java 16의 `Stream.toList()`는 `ImmutableCollections$ListN`을 반환한다. 이는 package-private 클래스라 Jackson이 역직렬화 시 생성자에 접근할 수 없다. Redis 캐시 반환값은 `new ArrayList<>()`로 명시적 생성이 필요하다.

**3. ObjectMapper 직접 생성 시 모듈 자동 등록 없음**
Spring Boot의 `ObjectMapper` 빈은 `ParameterNamesModule`, `JavaTimeModule` 등을 자동 등록해준다. 하지만 `CacheConfig`에서 `new ObjectMapper()`로 직접 생성하면 이 모듈들을 수동으로 등록해야 한다.

**4. 인덱스는 쿼리 패턴 단위로 설계해야 한다**
`(brand_id, like_count DESC)` 하나만 있으면 `latest`/`price_asc` 정렬은 여전히 filesort가 발생한다. 조회 조건(WHERE) × 정렬 조건(ORDER BY)의 조합을 모두 분석해서 인덱스를 설계해야 한다. 단, 인덱스가 많아질수록 쓰기 오버헤드가 증가하므로 실제 트래픽 패턴과 Read/Write 비율을 함께 고려해야 한다.

**5. k6 포트 확인 필수**
Spring Boot의 `management.server.port`는 Actuator 전용 포트다. 앱 API와 Actuator가 다른 포트를 사용하는 경우 k6 `BASE_URL`을 반드시 API 포트로 지정해야 한다.

**6. `ddl-auto: create` 환경에서의 테스트 워크플로우**
로컬 개발 편의를 위해 `create`를 사용하면 앱 재시작마다 스키마와 데이터가 초기화된다. 대용량 시드 데이터가 필요한 성능 테스트 환경에서는 `validate` 또는 `update`로 변경하거나, 재시작 후 자동 시드 삽입 스크립트를 준비해두어야 한다.

**7. MCP로 개발 생산성 향상**
MySQL MCP 서버를 Claude Code에 연결하면 `docker exec` 없이 자연어로 EXPLAIN 분석, 데이터 확인이 가능해진다. Docker가 실행 중일 때만 연결되므로 인프라가 먼저 떠있어야 한다.
