package com.loopers.domain.payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Optional<Payment> findById(Long id);

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findAllByStatus(PaymentStatus status);

    Payment save(Payment payment);
}
