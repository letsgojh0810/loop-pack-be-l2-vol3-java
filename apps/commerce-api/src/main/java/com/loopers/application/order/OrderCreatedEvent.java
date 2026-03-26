package com.loopers.application.order;

public record OrderCreatedEvent(Long orderId, Long userId) {
}
