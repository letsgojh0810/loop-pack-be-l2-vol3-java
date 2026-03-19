package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentCancelRequest;
import com.loopers.domain.payment.PaymentCancelRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentCancelRequestJpaRepository extends JpaRepository<PaymentCancelRequest, Long> {

    List<PaymentCancelRequest> findAllByStatus(PaymentCancelRequestStatus status);
}
