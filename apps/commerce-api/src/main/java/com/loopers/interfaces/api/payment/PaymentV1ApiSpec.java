package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Payment", description = "결제 API")
public interface PaymentV1ApiSpec {

    @Operation(summary = "결제 요청", description = "주문에 대한 결제를 요청합니다.")
    ApiResponse<PaymentV1Dto.PaymentResponse> requestPayment(
        @Parameter(description = "로그인 ID", required = true) String loginId,
        @Parameter(description = "비밀번호", required = true) String password,
        PaymentV1Dto.CreateRequest request
    );

    @Operation(summary = "PG 콜백 수신", description = "PG사로부터 결제 결과 콜백을 수신합니다.")
    ApiResponse<Void> handleCallback(
        @Parameter(description = "PG 검증 헤더") String pgVerifyHeader,
        PaymentV1Dto.CallbackRequest request
    );
}
