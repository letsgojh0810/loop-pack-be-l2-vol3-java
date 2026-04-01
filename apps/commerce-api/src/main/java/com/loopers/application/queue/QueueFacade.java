package com.loopers.application.queue;

import com.loopers.domain.queue.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class QueueFacade {

    private final QueueService queueService;

    public QueueInfo enter(Long userId) {
        long position = queueService.enter(userId);
        long waitingCount = queueService.getWaitingCount();
        long estimatedWait = queueService.estimateWaitSeconds(userId);
        return new QueueInfo(position, waitingCount, estimatedWait, null);
    }

    public QueueInfo getPosition(Long userId) {
        Long position = queueService.getPosition(userId);
        long waitingCount = queueService.getWaitingCount();

        if (position == null) {
            // 대기열에 없음 → 토큰 발급 여부 확인
            String token = queueService.getToken(userId);
            return new QueueInfo(null, waitingCount, 0, token);
        }

        long estimatedWait = queueService.estimateWaitSeconds(userId);
        return new QueueInfo(position, waitingCount, estimatedWait, null);
    }
}
