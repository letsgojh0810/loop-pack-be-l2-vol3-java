package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderItemInfo;
import com.loopers.application.order.OrderCreateItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderV1Dto {

    public record CreateRequest(
        @NotEmpty(message = "주문 상품 목록은 비어있을 수 없습니다.")
        @Valid
        List<OrderItemRequest> items
    ) {
        public List<OrderCreateItem> toOrderCreateItems() {
            return items.stream()
                .map(item -> new OrderCreateItem(item.productId(), item.quantity()))
                .toList();
        }
    }

    public record OrderItemRequest(
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        int quantity
    ) {}

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
