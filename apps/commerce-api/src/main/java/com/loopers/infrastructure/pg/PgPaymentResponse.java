package com.loopers.infrastructure.pg;

public record PgPaymentResponse(
    String pgTransactionId,
    String status, // "ACCEPTED", "SUCCESS", "LIMIT_EXCEEDED", "INVALID_CARD"
    String message
) {}
