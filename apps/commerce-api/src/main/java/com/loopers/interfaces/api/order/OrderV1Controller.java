package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private static final String HEADER_LOGIN_ID = "X-Loopers-LoginId";
    private static final String HEADER_LOGIN_PW = "X-Loopers-LoginPw";

    private final UserFacade userFacade;
    private final OrderFacade orderFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> createOrder(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password,
        @Valid @RequestBody OrderV1Dto.CreateRequest request
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);
        OrderInfo info = orderFacade.createOrder(currentUser.id(), request.toOrderCreateItems());
        return ApiResponse.success(OrderV1Dto.OrderResponse.from(info));
    }

    @GetMapping
    @Override
    public ApiResponse<OrderV1Dto.OrderListResponse> getMyOrders(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);
        List<OrderInfo> infos = orderFacade.getOrdersByUserId(currentUser.id());
        return ApiResponse.success(OrderV1Dto.OrderListResponse.from(infos));
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderV1Dto.OrderResponse> getOrder(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password,
        @PathVariable Long orderId
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);
        OrderInfo info = orderFacade.getOrder(orderId);
        if (!info.userId().equals(currentUser.id())) {
            throw new CoreException(ErrorType.FORBIDDEN, "본인의 주문만 조회할 수 있습니다.");
        }
        return ApiResponse.success(OrderV1Dto.OrderResponse.from(info));
    }
}
