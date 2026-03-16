package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentInfo;
import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.payment.CardType;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentV1Controller implements PaymentV1ApiSpec {

    private static final String HEADER_LOGIN_ID = "X-Loopers-LoginId";
    private static final String HEADER_LOGIN_PW = "X-Loopers-LoginPw";
    private static final String HEADER_PG_VERIFY = "X-USER-ID";

    private final UserFacade userFacade;
    private final PaymentFacade paymentFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<PaymentV1Dto.PaymentResponse> requestPayment(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password,
        @Valid @RequestBody PaymentV1Dto.CreateRequest request
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);

        CardType cardType;
        try {
            cardType = CardType.valueOf(request.cardType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "지원하지 않는 카드 종류입니다: " + request.cardType());
        }

        PaymentInfo info = paymentFacade.requestPayment(
            currentUser.id(), request.orderId(), cardType, request.cardNo()
        );
        return ApiResponse.success(PaymentV1Dto.PaymentResponse.from(info));
    }

    @PostMapping("/callback")
    @Override
    public ApiResponse<Void> handleCallback(
        @RequestHeader(value = HEADER_PG_VERIFY, required = false) String pgVerifyHeader,
        @Valid @RequestBody PaymentV1Dto.CallbackRequest request
    ) {
        paymentFacade.handleCallback(
            request.transactionKey(),
            request.status(),
            request.orderId(),
            request.cardType(),
            request.cardNo(),
            request.amount(),
            request.reason()
        );
        return ApiResponse.success(null);
    }
}
