package com.loopers.confg.kafka.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CouponIssueRequestMessage(
        String requestId,
        Long couponId,
        Long userId,
        long occurredAtEpoch
) {}
