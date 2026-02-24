package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderItemInfo;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderAdminV1Dto {

    public record OrderItemResponse(
        Long orderItemId,
        Long productId,
        String brandName,
        String productName,
        int price,
        int quantity
    ) {
        public static OrderItemResponse from(OrderItemInfo info) {
            return new OrderItemResponse(
                info.orderItemId(),
                info.productId(),
                info.brandName(),
                info.productName(),
                info.price(),
                info.quantity()
            );
        }
    }

    public record OrderResponse(
        Long orderId,
        Long userId,
        int totalAmount,
        List<OrderItemResponse> items,
        ZonedDateTime createdAt
    ) {
        public static OrderResponse from(OrderInfo info) {
            return new OrderResponse(
                info.orderId(),
                info.userId(),
                info.totalAmount(),
                info.items().stream()
                    .map(OrderItemResponse::from)
                    .toList(),
                info.createdAt()
            );
        }
    }

    public record OrderListResponse(
        List<OrderResponse> orders
    ) {
        public static OrderListResponse from(List<OrderInfo> infos) {
            return new OrderListResponse(
                infos.stream()
                    .map(OrderResponse::from)
                    .toList()
            );
        }
    }
}
