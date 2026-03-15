package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.ProductService;
import com.loopers.infrastructure.pg.PgClient;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ProductService productService;
    private final PgClient pgClient;

    @Transactional
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

        // TODO: PG 연동 후 실제 결제 요청 처리 필요
        // PgPaymentRequest pgRequest = new PgPaymentRequest(
        //     orderId, cardType.name(), cardNo, order.getTotalAmount(), callbackUrl
        // );
        // PgPaymentResponse pgResponse = pgClient.requestPayment(pgRequest);

        return PaymentInfo.from(payment);
    }

    @Transactional
    public void handleCallback(String pgTransactionId, String status, String orderId, String message) {
        // 콜백 처리: PG 결과에 따라 Payment/Order 상태 업데이트
        Long orderIdLong = Long.parseLong(orderId);
        Payment payment = paymentService.getPaymentByOrderId(orderIdLong);

        if ("SUCCESS".equals(status)) {
            // SUCCESS → completePayment + completeOrder + decreaseStock
            paymentService.completePayment(payment.getId(), pgTransactionId);
            orderService.completeOrder(orderIdLong);

            Order order = orderService.getOrder(orderIdLong);
            for (OrderItem item : order.getItems()) {
                productService.decreaseStock(item.getProductId(), item.getQuantity());
            }
        } else {
            // 그 외 → failPayment + cancelOrder
            paymentService.failPayment(payment.getId(), message);
            orderService.cancelOrder(orderIdLong);
        }
    }

    @Transactional
    public void syncPendingPayments() {
        // @Scheduled에서 호출 - PENDING 건들 PG 조회 후 상태 동기화
        // TODO: PG 연동 후 실제 동기화 구현 필요
        List<Payment> pendingPayments = paymentService.getPendingPayments();
        log.info("PENDING 결제 동기화 대상: {}건", pendingPayments.size());

        // for (Payment payment : pendingPayments) {
        //     PgPaymentResponse response = pgClient.getPaymentByOrderId(payment.getOrderId());
        //     if ("SUCCESS".equals(response.status())) {
        //         paymentService.completePayment(payment.getId(), response.pgTransactionId());
        //         orderService.completeOrder(payment.getOrderId());
        //     } else if ("LIMIT_EXCEEDED".equals(response.status()) || "INVALID_CARD".equals(response.status())) {
        //         paymentService.failPayment(payment.getId(), response.message());
        //         orderService.cancelOrder(payment.getOrderId());
        //     }
        // }
    }
}
