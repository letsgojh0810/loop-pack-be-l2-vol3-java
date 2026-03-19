package com.loopers.infrastructure.pg;

public record PgTransactionDetailResponse(
    String transactionKey,
    String orderId,
    String cardType,
    Long amount,
    String status,
    String reason
) {}
