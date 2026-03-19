package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_cancel_requests")
public class PaymentCancelRequest extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "pg_transaction_id")
    private String pgTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentCancelRequestStatus status;

    @Column(name = "reason")
    private String reason;

    protected PaymentCancelRequest() {}

    private PaymentCancelRequest(Long orderId, String pgTransactionId, String reason) {
        this.orderId = orderId;
        this.pgTransactionId = pgTransactionId;
        this.status = PaymentCancelRequestStatus.PENDING;
        this.reason = reason;
    }

    public static PaymentCancelRequest create(Long orderId, String pgTransactionId, String reason) {
        return new PaymentCancelRequest(orderId, pgTransactionId, reason);
    }

    public void complete() {
        this.status = PaymentCancelRequestStatus.SUCCESS;
    }

    public void fail() {
        this.status = PaymentCancelRequestStatus.FAILED;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getPgTransactionId() {
        return pgTransactionId;
    }

    public PaymentCancelRequestStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}
