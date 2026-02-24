package com.loopers.application.order;

import com.loopers.domain.order.Order;

import java.time.ZonedDateTime;
import java.util.List;

public record OrderInfo(
    Long orderId,
    Long userId,
    int totalAmount,
    List<OrderItemInfo> items,
    ZonedDateTime createdAt
) {
    public static OrderInfo of(Order order, List<OrderItemInfo> items) {
        return new OrderInfo(
            order.getId(),
            order.getUserId(),
            order.getTotalAmount(),
            items,
            order.getCreatedAt()
        );
    }
}
