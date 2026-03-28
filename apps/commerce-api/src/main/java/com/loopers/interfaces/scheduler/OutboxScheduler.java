package com.loopers.interfaces.scheduler;

import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxScheduler {

    private final OutboxEventService outboxEventService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventService.getPendingEvents();
        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getPartitionKey(), event.getPayload()).get();
                outboxEventService.markPublished(event.getId());
            } catch (Exception e) {
                log.warn("Outbox 발행 실패: eventId={}, retryCount={}, error={}",
                    event.getEventId(), event.getRetryCount(), e.getMessage());
                boolean isFailed = outboxEventService.incrementRetryAndCheckIfFailed(event.getId());
                if (isFailed) {
                    sendToDlq(event);
                }
            }
        }
    }

    private void sendToDlq(OutboxEvent event) {
        String dlqTopic = event.getTopic() + "-dlq";
        try {
            kafkaTemplate.send(dlqTopic, event.getPartitionKey(), event.getPayload()).get();
            log.warn("Outbox DLQ 발행 완료: eventId={}, dlqTopic={}", event.getEventId(), dlqTopic);
        } catch (Exception dlqEx) {
            log.error("Outbox DLQ 발행 실패: eventId={}, dlqTopic={}, error={}",
                event.getEventId(), dlqTopic, dlqEx.getMessage());
        }
    }
}
