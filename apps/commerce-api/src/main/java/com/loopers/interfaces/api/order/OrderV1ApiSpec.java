package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Order", description = "주문 API")
public interface OrderV1ApiSpec {

    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    ApiResponse<OrderV1Dto.OrderResponse> createOrder(
        @Parameter(description = "로그인 ID", required = true) String loginId,
        @Parameter(description = "비밀번호", required = true) String password,
        OrderV1Dto.CreateRequest request
    );

    @Operation(summary = "내 주문 목록 조회", description = "로그인한 사용자의 주문 목록을 조회합니다.")
    ApiResponse<OrderV1Dto.OrderListResponse> getMyOrders(
        @Parameter(description = "로그인 ID", required = true) String loginId,
        @Parameter(description = "비밀번호", required = true) String password
    );

    @Operation(summary = "주문 상세 조회", description = "주문 ID로 주문 상세 정보를 조회합니다.")
    ApiResponse<OrderV1Dto.OrderResponse> getOrder(
        @Parameter(description = "로그인 ID", required = true) String loginId,
        @Parameter(description = "비밀번호", required = true) String password,
        @Parameter(description = "주문 ID", required = true) Long orderId
    );
}
