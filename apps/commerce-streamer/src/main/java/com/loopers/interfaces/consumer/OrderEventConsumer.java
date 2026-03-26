package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.confg.kafka.KafkaConfig;
import com.loopers.confg.kafka.message.OrderEventMessage;
import com.loopers.domain.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;

    @KafkaListener(topics = "order-events", containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consume(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        for (ConsumerRecord<String, byte[]> record : records) {
            try {
                OrderEventMessage msg = objectMapper.readValue(record.value(), OrderEventMessage.class);
                if (!"ORDER_COMPLETED".equals(msg.eventType())) {
                    continue;
                }
                if (metricsService.isHandled(msg.eventId())) {
                    continue;
                }
                for (OrderEventMessage.Item item : msg.items()) {
                    metricsService.upsertSales(item.productId(), msg.version(), item.quantity());
                }
                metricsService.markHandled(msg.eventId(), msg.eventType());
            } catch (Exception e) {
                log.warn("order-events 처리 실패: offset={}", record.offset(), e);
            }
        }
        ack.acknowledge();
    }
}
