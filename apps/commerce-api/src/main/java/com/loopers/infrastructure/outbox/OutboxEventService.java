package com.loopers.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(String eventId, String topic, String partitionKey, Object payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox 직렬화 실패: " + e.getMessage(), e);
        }
        OutboxEvent event = OutboxEvent.create(eventId, topic, partitionKey, json);
        outboxEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<OutboxEvent> getPendingEvents() {
        return outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void markPublished(Long id) {
        outboxEventRepository.findById(id).ifPresent(OutboxEvent::markPublished);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void markFailed(Long id) {
        outboxEventRepository.findById(id).ifPresent(OutboxEvent::markFailed);
    }
}
