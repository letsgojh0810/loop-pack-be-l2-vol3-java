package com.loopers.confg.kafka.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderEventMessage(
        String eventId,
        String eventType,
        Long orderId,
        Long userId,
        List<Item> items,
        long occurredAtEpoch,
        long version
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(Long productId, int quantity) {}
}
