package com.loopers.application.order;

import com.loopers.domain.order.OrderItem;

public record OrderItemInfo(
    Long orderItemId,
    Long productId,
    String brandName,
    String productName,
    int price,
    int quantity
) {
    public static OrderItemInfo from(OrderItem item) {
        return new OrderItemInfo(
            item.getId(),
            item.getProductId(),
            item.getBrandName(),
            item.getProductName(),
            item.getPrice(),
            item.getQuantity()
        );
    }
}
