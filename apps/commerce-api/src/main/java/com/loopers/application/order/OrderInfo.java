package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;

import java.time.ZonedDateTime;
import java.util.List;

public record OrderInfo(
    Long orderId,
    Long userId,
    int originalAmount,
    int discountAmount,
    int totalAmount,
    Long userCouponId,
    OrderStatus status,
    List<OrderItemInfo> items,
    ZonedDateTime createdAt
) {
    public static OrderInfo of(Order order, List<OrderItemInfo> items) {
        return new OrderInfo(
            order.getId(),
            order.getUserId(),
            order.getOriginalAmount(),
            order.getDiscountAmount(),
            order.getTotalAmount(),
            order.getUserCouponId(),
            order.getStatus(),
            items,
            order.getCreatedAt()
        );
    }
}
