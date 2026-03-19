package com.loopers.application.payment;

import com.loopers.domain.payment.Payment;

import java.time.ZonedDateTime;

public record PaymentInfo(
    Long paymentId,
    Long orderId,
    Long userId,
    String cardType,
    int amount,
    String status,
    String pgTransactionId,
    ZonedDateTime createdAt
) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
            payment.getId(),
            payment.getOrderId(),
            payment.getUserId(),
            payment.getCardType().name(),
            payment.getAmount(),
            payment.getStatus().name(),
            payment.getPgTransactionId(),
            payment.getCreatedAt()
        );
    }
}
