package com.loopers.infrastructure.pg;

import org.springframework.stereotype.Component;

@Component
public class PgClientStub implements PgClient {

    @Override
    public PgPaymentResponse requestPayment(PgPaymentRequest request) {
        throw new UnsupportedOperationException("PG 연동이 아직 구현되지 않았습니다.");
    }

    @Override
    public PgPaymentResponse getPaymentStatus(String pgTransactionId) {
        throw new UnsupportedOperationException("PG 연동이 아직 구현되지 않았습니다.");
    }

    @Override
    public PgPaymentResponse getPaymentByOrderId(Long orderId) {
        throw new UnsupportedOperationException("PG 연동이 아직 구현되지 않았습니다.");
    }
}
