package com.loopers.infrastructure.pg;

public record PgPaymentResponse(
    String transactionKey,
    String status, // "ACCEPTED", "SUCCESS", "LIMIT_EXCEEDED", "INVALID_CARD"
    String reason
) {}
