package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.product.ProductService;
import com.loopers.infrastructure.pg.PgClient;
import com.loopers.infrastructure.pg.PgPaymentRequest;
import com.loopers.infrastructure.pg.PgPaymentResponse;
import com.loopers.infrastructure.pg.PgTransactionDetailResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private static final String CALLBACK_URL = "http://localhost:8080/api/v1/payments/callback";

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ProductService productService;
    private final PgClient pgClient;

    @Transactional
    @CircuitBreaker(name = "pgCircuitBreaker", fallbackMethod = "requestPaymentFallback")
    @Retry(name = "pgRetry")
    public PaymentInfo requestPayment(Long userId, Long orderId, CardType cardType, String cardNo) {
        // 1. 주문 존재 확인 + 본인 주문인지 + PENDING_PAYMENT 상태인지 확인
        Order order = orderService.getOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new CoreException(ErrorType.FORBIDDEN, "본인의 주문만 결제할 수 있습니다.");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 대기 상태의 주문만 결제할 수 있습니다.");
        }

        // 2. 이미 결제 요청한 건인지 확인 (중복 방지)
        paymentService.findPaymentByOrderId(orderId).ifPresent(existing -> {
            throw new CoreException(ErrorType.CONFLICT, "이미 결제 요청된 주문입니다.");
        });

        // 3. Payment(PENDING) 생성
        Payment payment = paymentService.createPayment(
            orderId, userId, cardType, cardNo, order.getTotalAmount()
        );

        // 4. PG 결제 요청
        PgPaymentRequest pgRequest = new PgPaymentRequest(
            String.valueOf(orderId),
            cardType.name(),
            cardNo,
            (long) order.getTotalAmount(),
            CALLBACK_URL
        );
        PgPaymentResponse pgResponse = pgClient.requestPayment(String.valueOf(userId), pgRequest);
        log.info("PG 결제 요청 결과: orderId={}, status={}", orderId, pgResponse.status());

        return PaymentInfo.from(payment);
    }

    @Transactional
    public PaymentInfo requestPaymentFallback(Long userId, Long orderId, CardType cardType, String cardNo, Throwable t) {
        log.warn("PG 결제 요청 실패 (fallback): orderId={}, error={}", orderId, t.getMessage());
        paymentService.findPaymentByOrderId(orderId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                paymentService.failPayment(payment.getId(), "PG 연결 실패: " + t.getMessage());
                orderService.cancelOrder(orderId);

                Order order = orderService.getOrder(orderId);
                for (OrderItem item : order.getItems()) {
                    productService.increaseStock(item.getProductId(), item.getQuantity());
                }
            }
        });
        throw new CoreException(ErrorType.INTERNAL_ERROR, "결제 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");
    }

    @Transactional
    public void handleCallback(String transactionKey, String status, String orderId, String cardType, String cardNo, Long amount, String reason) {
        // 콜백 처리: PG 결과에 따라 Payment/Order 상태 업데이트
        Long orderIdLong = Long.parseLong(orderId);
        Payment payment = paymentService.getPaymentByOrderId(orderIdLong);

        if ("SUCCESS".equals(status)) {
            // SUCCESS → completePayment + completeOrder (재고는 주문 생성 시 이미 차감됨)
            paymentService.completePayment(payment.getId(), transactionKey);
            orderService.completeOrder(orderIdLong);
        } else {
            // 그 외 → failPayment + cancelOrder + increaseStock (재고 복구)
            paymentService.failPayment(payment.getId(), reason);
            orderService.cancelOrder(orderIdLong);

            Order order = orderService.getOrder(orderIdLong);
            for (OrderItem item : order.getItems()) {
                productService.increaseStock(item.getProductId(), item.getQuantity());
            }
        }
    }

    @Transactional
    public void syncPendingPayments() {
        // @Scheduled에서 호출 - PENDING 건들 PG 조회 후 상태 동기화
        List<Payment> pendingPayments = paymentService.getPendingPayments();
        log.info("PENDING 결제 동기화 대상: {}건", pendingPayments.size());

        for (Payment payment : pendingPayments) {
            try {
                PgTransactionDetailResponse response = pgClient.getPaymentByOrderId(
                    String.valueOf(payment.getUserId()),
                    String.valueOf(payment.getOrderId())
                );
                if ("SUCCESS".equals(response.status())) {
                    paymentService.completePayment(payment.getId(), response.transactionKey());
                    orderService.completeOrder(payment.getOrderId());
                } else if ("LIMIT_EXCEEDED".equals(response.status()) || "INVALID_CARD".equals(response.status())) {
                    paymentService.failPayment(payment.getId(), response.reason());
                    orderService.cancelOrder(payment.getOrderId());

                    Order order = orderService.getOrder(payment.getOrderId());
                    for (OrderItem item : order.getItems()) {
                        productService.increaseStock(item.getProductId(), item.getQuantity());
                    }
                }
            } catch (Exception e) {
                log.warn("PENDING 결제 동기화 실패: paymentId={}, error={}", payment.getId(), e.getMessage());
            }
        }
    }
}
