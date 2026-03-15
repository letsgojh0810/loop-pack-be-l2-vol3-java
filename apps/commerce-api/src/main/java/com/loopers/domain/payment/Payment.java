package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Column(name = "card_no", nullable = false)
    private String cardNo;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "pg_transaction_id")
    private String pgTransactionId;

    @Column(name = "failure_reason")
    private String failureReason;

    protected Payment() {}

    private Payment(Long orderId, Long userId, CardType cardType, String cardNo, int amount) {
        if (orderId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 비어있을 수 없습니다.");
        }
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 비어있을 수 없습니다.");
        }
        if (cardType == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드 종류는 비어있을 수 없습니다.");
        }
        if (cardNo == null || cardNo.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드 번호는 비어있을 수 없습니다.");
        }
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 금액은 0보다 커야 합니다.");
        }
        this.orderId = orderId;
        this.userId = userId;
        this.cardType = cardType;
        this.cardNo = cardNo;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    public static Payment create(Long orderId, Long userId, CardType cardType, String cardNo, int amount) {
        return new Payment(orderId, userId, cardType, cardNo, amount);
    }

    public void complete(String pgTransactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "대기 중인 결제만 완료 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.SUCCESS;
        this.pgTransactionId = pgTransactionId;
    }

    public void fail(String reason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "대기 중인 결제만 실패 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public CardType getCardType() {
        return cardType;
    }

    public String getCardNo() {
        return cardNo;
    }

    public int getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getPgTransactionId() {
        return pgTransactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
