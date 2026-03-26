package com.loopers.confg.kafka.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogEventMessage(
        String eventId,
        String eventType,
        Long productId,
        Long userId,
        long occurredAtEpoch,
        long version
) {}
