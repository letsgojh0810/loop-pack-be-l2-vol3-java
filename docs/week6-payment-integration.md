# 6주차 작업 정리 — 결제 도메인 & PG 연동

## 1. 5주차 마무리 — 캐시 무효화 전략 변경

### 문제 인식
상품 좋아요(like/unlike) 발생 시 `evictProductCache()`를 호출해 상품 캐시 전체를 날리고 있었다.
좋아요는 빈번하게 바뀌는 데이터임에도 이벤트 기반 캐시 무효화를 적용하고 있었던 것.

### 결정: TTL 만료 전략으로 전환
| 이벤트 유형 | 변경 빈도 | 캐시 전략 |
|------------|---------|---------|
| 상품명 / 가격 수정 | 낮음 | 명시적 eviction (변경 즉시 반영 필요) |
| 좋아요 수 변동 | 높음 | TTL 만료 (일정 stale 허용) |

### 변경 내용
- `ProductLikeFacade`: `like()`, `unlike()`에서 `evictProductCache()` 호출 제거 및 `ProductFacade` 의존성 제거
- `ProductFacade`: `evictProductCache()` 메서드 자체 삭제

---

## 2. 결제 도메인 설계

### 핵심 설계 결정

| 항목 | 결정 | 이유 |
|------|------|------|
| 재고 차감 시점 | 결제 SUCCESS 콜백 수신 시 | 결제 전 재고 선점 → 미결제 시 재고 손실 문제 |
| 재고 동시성 제어 | DB 비관적 락 | 결제 완료는 저빈도, Redis 락 불필요 |
| PG 장애 처리 | CircuitBreaker + Retry + Fallback | PG 40% 실패율 대응 |
| 콜백 미수신 대응 | 30초 주기 스케줄러 폴링 | 콜백 유실 시 PENDING 건 자동 동기화 |
| 중복 결제 방지 | orderId 기준 기존 Payment 존재 확인 | 멱등성 보장 |

### Order 상태 기계
```
주문 생성 → PENDING_PAYMENT
              │
              ├─ 결제 SUCCESS 콜백 → PAID
              └─ 결제 FAILED / fallback → CANCELLED
```

---

## 3. 결제 도메인 구조

### 패키지 구성
```
domain/payment/
  ├── Payment.java              # 결제 엔티티
  ├── PaymentStatus.java        # PENDING / SUCCESS / FAILED
  ├── CardType.java             # SAMSUNG / KB / HYUNDAI
  ├── PaymentRepository.java    # 도메인 리포지토리 인터페이스
  └── PaymentService.java       # 도메인 서비스

domain/order/
  └── OrderStatus.java          # PENDING_PAYMENT / PAID / CANCELLED

infrastructure/payment/
  ├── PaymentJpaRepository.java
  └── PaymentRepositoryImpl.java

infrastructure/pg/
  ├── PgClient.java                   # @FeignClient
  ├── PgFeignConfig.java
  ├── PgPaymentRequest.java
  ├── PgPaymentResponse.java
  └── PgTransactionDetailResponse.java

infrastructure/scheduler/
  └── PaymentScheduler.java           # PENDING 결제 주기적 동기화

application/payment/
  ├── PaymentFacade.java              # 결제 흐름 조율
  └── PaymentInfo.java

interfaces/api/payment/
  ├── PaymentV1Controller.java
  └── PaymentV1Dto.java
```

### Payment 엔티티 핵심 규칙
- 생성 시 status = `PENDING` 고정
- `complete(pgTransactionId)` — PENDING 상태에서만 전이 가능
- `fail(reason)` — PENDING 상태에서만 전이 가능

---

## 4. PG 연동 상세

### PG 시뮬레이터 스펙
- 포트: `8082`
- 인증: `X-USER-ID` 헤더
- 결제 요청: `POST /api/v1/payments` (100~500ms 지연, 40% 실패율)
- 단건 조회: `GET /api/v1/payments/{transactionKey}`
- 주문별 조회: `GET /api/v1/payments?orderId=xxx`
- 콜백 body (TransactionInfo): `{ transactionKey, orderId, cardType, cardNo, amount, status, reason }`
- callbackUrl 제약: `http://localhost:8080`으로 시작해야 함

### Resilience4j 설정
```yaml
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
```

### PaymentFacade 흐름

```
requestPayment()
  @CircuitBreaker + @Retry
  ├── 주문 소유자 및 상태(PENDING_PAYMENT) 검증
  ├── 중복 결제 체크 (orderId)
  ├── Payment(PENDING) 생성
  └── PG POST /api/v1/payments 호출

requestPaymentFallback()  ← CB Open 또는 Retry 소진 시
  └── Payment → FAILED, Order → CANCELLED

handleCallback()  ← PG가 결제 결과를 push
  ├── SUCCESS → Payment/Order COMPLETE + decreaseStock
  └── FAILED  → Payment/Order CANCEL

syncPendingPayments()  ← 매 30초 스케줄러 호출
  └── PENDING 건 PG 조회 → 상태 동기화
```

### OrderFacade 변경
- `createOrder()`에서 `decreaseStock()` 제거
- 재고 부족 검사(check)는 유지 — 주문 자체는 막되, 실제 차감은 결제 완료 시점으로 이관

---

## 5. Git 커밋 이력

```
199da85f  feat: PG FeignClient + Resilience4j 결제 연동 구현
ca0c89ec  feat: 결제 도메인 구조 구현 (PG 연동 전)
1ac71164  docs: 좋아요 캐시 무효화 전략 변경 내용 및 인사이트 추가
b045efa4  refactor: 좋아요 변경 시 캐시 무효화 제거 - TTL 만료 전략으로 전환
```

브랜치: `feat/payment` → 포크 레포 push 완료
