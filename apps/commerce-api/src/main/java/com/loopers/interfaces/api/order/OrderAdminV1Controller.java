package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/orders")
public class OrderAdminV1Controller implements OrderAdminV1ApiSpec {

    private static final String ADMIN_LDAP_HEADER = "X-Loopers-Ldap";
    private static final String ADMIN_LDAP_VALUE = "loopers.admin";

    private final OrderFacade orderFacade;

    private void validateAdmin(String ldap) {
        if (!ADMIN_LDAP_VALUE.equals(ldap)) {
            throw new CoreException(ErrorType.FORBIDDEN, "어드민 권한이 필요합니다.");
        }
    }

    @GetMapping
    @Override
    public ApiResponse<OrderAdminV1Dto.OrderListResponse> getAllOrders(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap
    ) {
        validateAdmin(ldap);
        List<OrderInfo> infos = orderFacade.getAllOrders();
        return ApiResponse.success(OrderAdminV1Dto.OrderListResponse.from(infos));
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<OrderAdminV1Dto.OrderResponse> getOrder(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @PathVariable Long orderId
    ) {
        validateAdmin(ldap);
        OrderInfo info = orderFacade.getOrder(orderId);
        return ApiResponse.success(OrderAdminV1Dto.OrderResponse.from(info));
    }
}
