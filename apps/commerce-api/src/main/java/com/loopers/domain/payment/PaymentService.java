package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment createPayment(Long orderId, Long userId, CardType cardType, String cardNo, int amount) {
        Payment payment = Payment.create(orderId, userId, cardType, cardNo, amount);
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 내역을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "해당 주문의 결제 내역을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPendingPayments() {
        return paymentRepository.findAllByStatus(PaymentStatus.PENDING);
    }

    @Transactional
    public void completePayment(Long paymentId, String pgTransactionId) {
        Payment payment = getPayment(paymentId);
        payment.complete(pgTransactionId);
    }

    @Transactional
    public void failPayment(Long paymentId, String reason) {
        Payment payment = getPayment(paymentId);
        payment.fail(reason);
    }
}
