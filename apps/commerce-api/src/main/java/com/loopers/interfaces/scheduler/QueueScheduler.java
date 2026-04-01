package com.loopers.interfaces.scheduler;

import com.loopers.domain.queue.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueueScheduler {

    private final QueueService queueService;

    @Scheduled(fixedDelay = 100)
    public void issueTokens() {
        if (!queueService.isEnabled()) return;

        int batchSize = queueService.getBatchSize();
        List<Long> issued = queueService.issueTokens(batchSize);

        if (!issued.isEmpty()) {
            log.info("입장 토큰 발급: {}명 (대기열 잔여: {}명)", issued.size(), queueService.getWaitingCount());
        }
    }
}
