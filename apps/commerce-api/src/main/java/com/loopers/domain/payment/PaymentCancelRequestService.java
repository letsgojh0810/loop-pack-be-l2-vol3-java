package com.loopers.domain.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PaymentCancelRequestService {

    private final PaymentCancelRequestRepository paymentCancelRequestRepository;

    @Transactional
    public PaymentCancelRequest createCancelRequest(Long orderId, String pgTransactionId, String reason) {
        PaymentCancelRequest cancelRequest = PaymentCancelRequest.create(orderId, pgTransactionId, reason);
        return paymentCancelRequestRepository.save(cancelRequest);
    }

    @Transactional(readOnly = true)
    public List<PaymentCancelRequest> getPendingCancelRequests() {
        return paymentCancelRequestRepository.findAllByStatus(PaymentCancelRequestStatus.PENDING);
    }

    @Transactional
    public void completeCancelRequest(PaymentCancelRequest cancelRequest) {
        cancelRequest.complete();
        paymentCancelRequestRepository.save(cancelRequest);
    }

    @Transactional
    public void failCancelRequest(PaymentCancelRequest cancelRequest) {
        cancelRequest.fail();
        paymentCancelRequestRepository.save(cancelRequest);
    }
}
