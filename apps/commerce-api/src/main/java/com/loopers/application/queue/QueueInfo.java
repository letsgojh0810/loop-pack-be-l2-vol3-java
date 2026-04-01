package com.loopers.application.queue;

public record QueueInfo(
    Long position,
    long waitingCount,
    long estimatedWaitSeconds,
    String token
) {}
