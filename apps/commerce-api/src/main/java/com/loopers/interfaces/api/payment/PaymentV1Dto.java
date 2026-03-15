package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public class PaymentV1Dto {

    public record CreateRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        Long orderId,

        @NotBlank(message = "카드 종류는 필수입니다.")
        String cardType,

        @NotBlank(message = "카드 번호는 필수입니다.")
        String cardNo
    ) {}

    public record CallbackRequest(
        @NotBlank(message = "PG 트랜잭션 ID는 필수입니다.")
        String pgTransactionId,

        @NotBlank(message = "결제 상태는 필수입니다.")
        String status,

        @NotBlank(message = "주문 ID는 필수입니다.")
        String orderId,

        String message
    ) {}

    public record PaymentResponse(
        Long paymentId,
        Long orderId,
        Long userId,
        String cardType,
        int amount,
        String status,
        String pgTransactionId,
        ZonedDateTime createdAt
    ) {
        public static PaymentResponse from(PaymentInfo info) {
            return new PaymentResponse(
                info.paymentId(),
                info.orderId(),
                info.userId(),
                info.cardType(),
                info.amount(),
                info.status(),
                info.pgTransactionId(),
                info.createdAt()
            );
        }
    }
}
