package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Order Admin", description = "주문 어드민 API")
public interface OrderAdminV1ApiSpec {

    @Operation(summary = "전체 주문 목록 조회", description = "모든 주문 목록을 조회합니다.")
    ApiResponse<OrderAdminV1Dto.OrderListResponse> getAllOrders(
        @Parameter(description = "어드민 LDAP", required = true) String ldap
    );

    @Operation(summary = "주문 상세 조회", description = "주문 ID로 주문 상세 정보를 조회합니다.")
    ApiResponse<OrderAdminV1Dto.OrderResponse> getOrder(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "주문 ID", required = true) Long orderId
    );
}
