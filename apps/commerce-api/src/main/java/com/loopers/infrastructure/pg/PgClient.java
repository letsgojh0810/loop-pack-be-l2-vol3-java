package com.loopers.infrastructure.pg;

public interface PgClient {

    PgPaymentResponse requestPayment(PgPaymentRequest request);

    PgPaymentResponse getPaymentStatus(String pgTransactionId);

    PgPaymentResponse getPaymentByOrderId(Long orderId);
}
