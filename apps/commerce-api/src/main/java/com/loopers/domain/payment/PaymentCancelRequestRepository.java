package com.loopers.domain.payment;

import java.util.List;

public interface PaymentCancelRequestRepository {

    PaymentCancelRequest save(PaymentCancelRequest cancelRequest);

    List<PaymentCancelRequest> findAllByStatus(PaymentCancelRequestStatus status);
}
