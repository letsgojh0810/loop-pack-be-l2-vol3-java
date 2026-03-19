package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentCancelRequest;
import com.loopers.domain.payment.PaymentCancelRequestRepository;
import com.loopers.domain.payment.PaymentCancelRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PaymentCancelRequestRepositoryImpl implements PaymentCancelRequestRepository {

    private final PaymentCancelRequestJpaRepository paymentCancelRequestJpaRepository;

    @Override
    public PaymentCancelRequest save(PaymentCancelRequest cancelRequest) {
        return paymentCancelRequestJpaRepository.save(cancelRequest);
    }

    @Override
    public List<PaymentCancelRequest> findAllByStatus(PaymentCancelRequestStatus status) {
        return paymentCancelRequestJpaRepository.findAllByStatus(status);
    }
}
