package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.confg.kafka.KafkaConfig;
import com.loopers.confg.kafka.message.CatalogEventMessage;
import com.loopers.domain.metrics.MetricsService;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CatalogEventConsumer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;
    private final RankingService rankingService;

    @KafkaListener(topics = "catalog-events", containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consume(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        for (ConsumerRecord<String, byte[]> record : records) {
            try {
                CatalogEventMessage msg = objectMapper.readValue(record.value(), CatalogEventMessage.class);
                if (metricsService.isHandled(msg.eventId())) {
                    continue;
                }
                String date = toDate(msg.occurredAtEpoch());
                switch (msg.eventType()) {
                    case "LIKE_CREATED" -> {
                        metricsService.upsertLike(msg.productId(), msg.version(), 1);
                        rankingService.addLikeScore(date, msg.productId(), 1);
                    }
                    case "LIKE_CANCELLED" -> {
                        metricsService.upsertLike(msg.productId(), msg.version(), -1);
                        rankingService.addLikeScore(date, msg.productId(), -1);
                    }
                    case "PRODUCT_VIEWED" -> {
                        metricsService.upsertView(msg.productId(), msg.version());
                        rankingService.addViewScore(date, msg.productId());
                    }
                }
                metricsService.markHandled(msg.eventId(), msg.eventType());
            } catch (Exception e) {
                log.warn("catalog-events 처리 실패: offset={}", record.offset(), e);
            }
        }
        ack.acknowledge();
    }

    private String toDate(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli)
                .atZone(ZoneId.systemDefault())
                .format(DATE_FORMATTER);
    }
}
