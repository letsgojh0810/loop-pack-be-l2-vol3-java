# 6주차 작업 정리 — 결제 도메인 & PG 연동

> 작업일: 2026-03-18

---

## 1. 5주차 마무리 — 캐시 무효화 전략 변경

### 배경 및 문제 인식

좋아요(like/unlike) 발생 시 `ProductLikeFacade`가 `evictProductCache()`를 호출해 **상품 캐시 전체**를 날리는 구조였다.
좋아요 수는 매우 빈번하게 바뀌는 데이터인데, 변경될 때마다 캐시를 무효화하면 캐싱 자체가 무의미해진다는 점을 인식했다.

### 캐시 무효화 전략 비교 및 결론

| 이벤트 유형 | 변경 빈도 | 적용 전략 | 이유 |
|------------|---------|---------|------|
| 상품명 / 가격 수정 | 낮음 | **명시적 eviction** | 즉시 반영이 중요한 데이터 |
| 좋아요 수 변동 | 높음 | **TTL 만료** | 어느 정도 stale 허용 가능, 무효화 비용이 더 큼 |

**stale이란?** 캐시에 저장된 데이터가 실제 DB의 최신 값과 다소 차이가 있는 상태.
좋아요 수는 수백 번 눌려도 1~2초 정도 오차가 있어도 사용자가 크게 불편을 느끼지 않으므로 TTL 만료 전략이 적절하다.

### 변경된 파일

**`application/like/ProductLikeFacade.java`**
- `like()`, `unlike()` 메서드에서 `evictProductCache()` 호출 제거
- `ProductFacade` 의존성 완전 제거 (생성자에서도 삭제)
- 변경 후 코드:
  ```java
  public void like(Long userId, Long productId) {
      productService.getProduct(productId);
      productLikeService.like(userId, productId);
      // 좋아요 수는 변경 빈도가 높아 캐시 무효화 대신 TTL 만료에 맡김
  }
  ```

**`application/product/ProductFacade.java`**
- `evictProductCache()` 메서드 자체 삭제

---

## 2. 6주차 과제 — 결제 도메인 설계

### 과제 요구사항
PG(Payment Gateway) 외부 시스템과의 연동을 Resilience4j로 안전하게 처리하는 결제 기능 구현.
필수 구현: **Timeout**, **CircuitBreaker**, **Fallback**, **Retry**

### 설계 전 Q&A로 결정한 핵심 사항들

#### 재고 차감 시점
- **기존 생각**: 주문 생성 시 재고 선점
- **문제점**: 주문만 하고 결제 안 하면 재고가 잠겨버림. 결제 완료된 사람이 재고가 없어서 결제 실패할 수 있음
- **결정**: **결제 SUCCESS 콜백 수신 시** 재고 차감
- **추가 처리**: 주문 생성 시 재고 부족 체크(check)는 유지하되, 실제 차감(decrease)은 결제 완료 시점으로 이관

#### 재고 동시성 제어 방식
- **후보 1**: Redis 분산 락 — 빠르지만 인프라 복잡도 증가
- **후보 2**: DB 비관적 락 — 결제 완료는 저빈도 이벤트라 충분히 감당 가능
- **결정**: **DB 비관적 락** (결제 완료는 빈번하지 않으므로 Redis 락 도입 불필요)

#### PG 장애 처리 방식
- PG 시뮬레이터가 **40% 실패율**, 100~500ms 지연을 가짐
- CircuitBreaker: 실패율 50% 초과 시 Open → 10초 후 Half-Open 시도
- Retry: 최대 3회, 500ms 간격으로 재시도
- Fallback: CB Open 또는 Retry 소진 시 결제 FAILED 처리 + 주문 CANCELLED

#### 콜백 미수신 대응
- PG가 결제 결과를 callbackUrl로 push하는데, 네트워크 문제 등으로 콜백이 유실될 수 있음
- **결정**: 30초마다 스케줄러가 PENDING 건들을 PG에 직접 조회해서 상태 동기화

#### 중복 결제 방지
- 같은 주문에 대해 결제 요청이 중복 발생할 수 있음
- **결정**: orderId를 멱등성 키로 사용, Payment 생성 전 orderId로 기존 건 존재 여부 확인

### Order 상태 기계

```
주문 생성
    │
    ▼
PENDING_PAYMENT  ← 기본 상태
    │
    ├─ 결제 SUCCESS 콜백 수신  ──▶  PAID
    │
    └─ 결제 FAILED 콜백 / fallback  ──▶  CANCELLED
```

---

## 3. 결제 도메인 구조 구현

### 전체 패키지 구성

```
apps/commerce-api/src/main/java/com/loopers/
│
├── domain/
│   ├── order/
│   │   ├── Order.java                    # 기존 파일 수정 — status 필드 추가
│   │   ├── OrderStatus.java              # 신규 — PENDING_PAYMENT / PAID / CANCELLED
│   │   └── OrderService.java             # 기존 파일 수정 — completeOrder, cancelOrder 추가
│   │
│   └── payment/
│       ├── Payment.java                  # 신규 — 결제 엔티티
│       ├── PaymentStatus.java            # 신규 — PENDING / SUCCESS / FAILED
│       ├── CardType.java                 # 신규 — SAMSUNG / KB / HYUNDAI
│       ├── PaymentRepository.java        # 신규 — 도메인 리포지토리 인터페이스
│       └── PaymentService.java           # 신규 — 도메인 서비스
│
├── infrastructure/
│   ├── payment/
│   │   ├── PaymentJpaRepository.java     # 신규 — JPA 인터페이스
│   │   └── PaymentRepositoryImpl.java    # 신규 — 리포지토리 구현체
│   │
│   ├── pg/
│   │   ├── PgClient.java                 # 신규 — @FeignClient
│   │   ├── PgFeignConfig.java            # 신규 — Feign 로거 설정
│   │   ├── PgPaymentRequest.java         # 신규 — PG 결제 요청 DTO
│   │   ├── PgPaymentResponse.java        # 신규 — PG 결제 응답 DTO
│   │   └── PgTransactionDetailResponse.java  # 신규 — PG 단건/주문별 조회 응답 DTO
│   │
│   └── scheduler/
│       └── PaymentScheduler.java         # 신규 — PENDING 결제 주기적 동기화
│
├── application/
│   ├── order/
│   │   └── OrderFacade.java              # 기존 파일 수정 — decreaseStock 제거
│   │
│   └── payment/
│       ├── PaymentFacade.java            # 신규 — 결제 흐름 조율 (핵심)
│       └── PaymentInfo.java              # 신규 — 결제 결과 DTO
│
└── interfaces/api/payment/
    ├── PaymentV1Controller.java          # 신규 — 결제 API 엔드포인트
    ├── PaymentV1ApiSpec.java             # 신규 — API 스펙 인터페이스
    └── PaymentV1Dto.java                 # 신규 — Request / Response DTO
```

---

### 도메인 레이어 상세

#### `Payment.java` — 결제 엔티티

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | Long | 연결된 주문 ID |
| userId | Long | 결제 요청한 사용자 |
| cardType | CardType | 카드 종류 (SAMSUNG/KB/HYUNDAI) |
| cardNo | String | 카드 번호 |
| amount | int | 결제 금액 |
| status | PaymentStatus | 현재 결제 상태 |
| pgTransactionId | String | PG에서 발급한 트랜잭션 키 (SUCCESS 시 채워짐) |
| failureReason | String | 실패 사유 (FAILED 시 채워짐) |

**핵심 규칙:**
- `Payment.create(...)` 팩토리 메서드로만 생성 가능 → 생성 시 status = `PENDING` 고정
- `complete(pgTransactionId)` — PENDING 상태에서만 호출 가능, SUCCESS로 전이
- `fail(reason)` — PENDING 상태에서만 호출 가능, FAILED로 전이
- 상태 전이 규칙 위반 시 `CoreException(BAD_REQUEST)` 발생

#### `OrderStatus.java` — 신규 추가

```java
public enum OrderStatus {
    PENDING_PAYMENT,  // 주문 생성 직후 기본값
    PAID,             // 결제 완료
    CANCELLED         // 결제 실패 또는 취소
}
```

#### `Order.java` — 기존 파일 수정 내용

추가된 것:
- `status` 필드 (`@Enumerated(STRING)`, 기본값: `PENDING_PAYMENT`)
- `getStatus()` getter
- `complete()` — PENDING_PAYMENT → PAID 전이, 그 외 상태에서 호출 시 예외
- `cancel()` — PAID 상태에서 호출 시 예외, 그 외는 CANCELLED로 전이

#### `OrderService.java` — 기존 파일 수정 내용

추가된 것:
- `completeOrder(Long orderId)` — 주문 조회 후 `order.complete()` 호출
- `cancelOrder(Long orderId)` — 주문 조회 후 `order.cancel()` 호출

#### `CardType.java`

PG 시뮬레이터 스펙에 맞춰 3개만 정의:
```java
public enum CardType { SAMSUNG, KB, HYUNDAI }
```

---

### 인프라 레이어 상세

#### PG 시뮬레이터 스펙 (분석 내용)

| 항목 | 내용 |
|------|------|
| 주소 | `http://localhost:8082` |
| 인증 | `X-USER-ID` 헤더 (모든 요청에 필수) |
| 결제 요청 | `POST /api/v1/payments` |
| 단건 조회 | `GET /api/v1/payments/{transactionKey}` |
| 주문별 조회 | `GET /api/v1/payments?orderId=xxx` |
| 처리 지연 | 100~500ms |
| 실패율 | 40% |
| callbackUrl 제약 | `http://localhost:8080`으로 시작해야 함 |
| 콜백 body 포맷 | `{ transactionKey, orderId, cardType, cardNo, amount, status, reason }` |
| status 값 | `ACCEPTED`, `SUCCESS`, `LIMIT_EXCEEDED`, `INVALID_CARD` |

#### `PgClient.java` — `@FeignClient`

```java
@FeignClient(name = "pg-client", url = "${pg.url}", configuration = PgFeignConfig.class)
public interface PgClient {
    @PostMapping("/api/v1/payments")
    PgPaymentResponse requestPayment(@RequestHeader("X-USER-ID") String userId,
                                     @RequestBody PgPaymentRequest request);

    @GetMapping("/api/v1/payments/{transactionKey}")
    PgTransactionDetailResponse getPaymentStatus(@RequestHeader("X-USER-ID") String userId,
                                                  @PathVariable String transactionKey);

    @GetMapping("/api/v1/payments")
    PgTransactionDetailResponse getPaymentByOrderId(@RequestHeader("X-USER-ID") String userId,
                                                     @RequestParam("orderId") String orderId);
}
```

#### `PgPaymentRequest.java`

```java
public record PgPaymentRequest(
    String orderId,       // Long이 아닌 String (PG 스펙)
    String cardType,
    String cardNo,
    Long amount,          // int가 아닌 Long (PG 스펙)
    String callbackUrl    // http://localhost:8080/api/v1/payments/callback
) {}
```

#### `PgPaymentResponse.java`

```java
public record PgPaymentResponse(
    String transactionKey,  // PG가 발급하는 트랜잭션 키
    String status,          // ACCEPTED / SUCCESS / LIMIT_EXCEEDED / INVALID_CARD
    String reason           // 실패 사유
) {}
```

#### `PaymentScheduler.java`

```java
@Scheduled(fixedDelay = 30000)  // 30초마다 실행
public void syncPendingPayments() {
    paymentFacade.syncPendingPayments();
}
```

---

### 애플리케이션 레이어 상세

#### `PaymentFacade.java` — 결제 흐름 핵심

**`requestPayment()` 흐름:**
```
1. 주문 존재 확인 + 본인 주문인지 검증
2. 주문 상태가 PENDING_PAYMENT인지 확인
3. orderId로 기존 Payment 존재 여부 확인 (중복 방지)
4. Payment(PENDING) DB 저장
5. PG에 결제 요청 (FeignClient)
   └─ 실패 시: @Retry(3회) → 소진 시 @CircuitBreaker fallback
```

**`requestPaymentFallback()` — CB Open / Retry 소진 시:**
```
- PENDING 상태의 Payment → FAILED 처리
- 해당 Order → CANCELLED 처리
- CoreException(INTERNAL_ERROR) 반환
```

**`handleCallback()` — PG 콜백 수신 시:**
```
status == "SUCCESS"
  └─ Payment.complete(transactionKey)
  └─ Order.complete()
  └─ 주문 항목별 decreaseStock() (비관적 락)

status != "SUCCESS"
  └─ Payment.fail(reason)
  └─ Order.cancel()
```

**`syncPendingPayments()` — 스케줄러 호출:**
```
1. DB에서 status == PENDING인 Payment 목록 조회
2. 각 건마다 PG에 orderId로 상태 조회
3. SUCCESS → complete 처리
4. LIMIT_EXCEEDED / INVALID_CARD → fail 처리
5. 조회 실패 시 개별 건 스킵 (로그 남김)
```

**Resilience4j 어노테이션 적용 방식:**
```java
@CircuitBreaker(name = "pgCircuitBreaker", fallbackMethod = "requestPaymentFallback")
@Retry(name = "pgRetry")
public PaymentInfo requestPayment(...) { ... }
```
- CB가 바깥, Retry가 안쪽으로 동작: Retry 소진 → CB가 Open 판단
- fallbackMethod 시그니처: 원본 메서드 파라미터 + `Throwable t` 추가

---

### 인터페이스 레이어 상세

#### `PaymentV1Controller.java` — API 엔드포인트

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/v1/payments` | 결제 요청 (사용자 인증 필요: X-Loopers-LoginId/Pw) |
| POST | `/api/v1/payments/callback` | PG 콜백 수신 (X-USER-ID 헤더) |

#### `PaymentV1Dto.java`

```java
// 결제 요청
record CreateRequest(Long orderId, String cardType, String cardNo)

// PG 콜백 (TransactionInfo 포맷 그대로)
record CallbackRequest(
    String transactionKey, String orderId, String cardType,
    String cardNo, Long amount, String status, String reason
)

// 결제 응답
record PaymentResponse(
    Long paymentId, Long orderId, Long userId, String cardType,
    int amount, String status, String pgTransactionId, ZonedDateTime createdAt
)
```

---

## 4. 설정 추가 내용

### `application.yml` 추가 항목

```yaml
# PG 연동 주소
pg:
  url: http://localhost:8082

# Feign 타임아웃 (Timeout 요구사항 충족)
feign:
  client:
    config:
      pg-client:
        connect-timeout: 1000   # 1초
        read-timeout: 3000      # 3초

# Resilience4j (CircuitBreaker + Retry + TimeLimiter)
resilience4j:
  circuitbreaker:
    instances:
      pgCircuitBreaker:
        sliding-window-size: 10
        failure-rate-threshold: 50        # 10회 중 5회 실패 시 Open
        wait-duration-in-open-state: 10s  # 10초 후 Half-Open 전환
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      pgRetry:
        max-attempts: 3
        wait-duration: 500ms
  timelimiter:
    instances:
      pgCircuitBreaker:
        timeout-duration: 3s              # 3초 초과 시 타임아웃
```

### `build.gradle.kts` 추가 의존성

```kotlin
implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
implementation("io.github.resilience4j:resilience4j-spring-boot3")
implementation("org.springframework.boot:spring-boot-starter-aop")
```

### `CommerceApiApplication.java` 수정

```java
@EnableFeignClients   // FeignClient 활성화
@EnableScheduling     // @Scheduled 활성화
```

---

## 5. 과제 요구사항 충족 여부

| 요구사항 | 구현 방식 | 상태 |
|---------|---------|------|
| **Timeout** | Feign read-timeout 3초 + TimeLimiter 3초 | ✅ |
| **CircuitBreaker** | `@CircuitBreaker(name="pgCircuitBreaker")` | ✅ |
| **Fallback** | `requestPaymentFallback()` 메서드 | ✅ |
| **Retry** | `@Retry(name="pgRetry")`, 최대 3회 | ✅ |
| **비동기 콜백 처리** | `POST /api/v1/payments/callback` | ✅ |
| **PENDING 동기화** | 30초 스케줄러 + PG 조회 | ✅ |
| **재고 차감 시점** | 결제 SUCCESS 콜백 수신 시 | ✅ |
| **중복 결제 방지** | orderId 멱등성 체크 | ✅ |

---

## 6. 주요 트러블슈팅

### `application.yml` 설정 미저장 문제
- executor agent가 설정 내용을 출력은 했지만 실제 파일에 저장이 안 됨
- `pg.url`, `feign`, `resilience4j` 설정 수동으로 재추가 후 해결

### `Order.java`, `OrderService.java` 미저장 문제
- 이전 세션에서 수정했다고 보고됐으나 실제 파일에 반영이 안 돼 있었음
- `getStatus()`, `complete()`, `cancel()` 메서드 / `completeOrder()`, `cancelOrder()` 수동 추가

### `feat/payment` 브랜치 cherry-pick 충돌
- main에서 수정한 내용을 feat/payment에 cherry-pick 시 충돌 발생
- 원인: feat/payment에 이미 일부 내용이 존재 + `PgClientStub.java`가 삭제되지 않은 상태
- 해결: 충돌 수동 해결 → 중복 메서드 제거 → PgClientStub 삭제 → clean build 성공

---

## 7. Git 커밋 이력 (6주차 전체)

```
efcb12d4  fix: OrderService 중복 메서드 제거 및 PgClientStub 삭제 (feat/payment)
875bfb88  fix: Order 상태 필드/메서드 추가 및 설정 누락 보완
d1351eb6  fix: Order 상태 필드/메서드 추가 및 설정 누락 보완 (main)
199da85f  feat: PG FeignClient + Resilience4j 결제 연동 구현
ca0c89ec  feat: 결제 도메인 구조 구현 (PG 연동 전)
1ac71164  docs: 좋아요 캐시 무효화 전략 변경 내용 및 인사이트 추가
b045efa4  refactor: 좋아요 변경 시 캐시 무효화 제거 - TTL 만료 전략으로 전환
```

**브랜치:**
- `main` — 포크 레포 push 완료
- `feat/payment` — 포크 레포 push 완료

---

## 8. 내일 멘토링 질문 목록

1. **재고 차감 시점** — 주문 생성 vs 결제 완료, 실제 서비스에서의 트레이드오프는?
2. **콜백 + 폴링 동시 사용** — 같은 결제를 두 경로가 동시에 처리할 경우 PENDING 체크만으로 충분한지?
3. **@CircuitBreaker + @Retry 순서** — CB 바깥 / Retry 안쪽이 맞는지, AOP 우선순위 제어 방법은?
4. **Fallback + @Transactional** — CB fallback 메서드에서 트랜잭션이 의도대로 전파되는지?
5. **PG 호출과 트랜잭션 경계** — Payment 생성 + PG HTTP 호출이 같은 트랜잭션이면 커넥션을 오래 잡는 문제가 생기는데, 어떻게 분리하는 게 좋은지?
6. **PENDING 만료 처리** — 스케줄러가 있어도 PG가 장기 무응답 시 PENDING이 영구 쌓이는 문제, 실무에서의 만료 기준은?
