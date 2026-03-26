package com.loopers.interfaces.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.confg.kafka.KafkaConfig;
import com.loopers.confg.kafka.message.CouponIssueRequestMessage;
import com.loopers.domain.coupon.CouponIssueService;
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
public class CouponIssueConsumer {

    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;
    private final CouponIssueService couponIssueService;

    @KafkaListener(topics = "coupon-issue-requests", containerFactory = KafkaConfig.BATCH_LISTENER)
    public void consume(List<ConsumerRecord<String, byte[]>> records, Acknowledgment ack) {
        for (ConsumerRecord<String, byte[]> record : records) {
            try {
                CouponIssueRequestMessage msg = objectMapper.readValue(record.value(), CouponIssueRequestMessage.class);

                if (metricsService.isHandled(msg.requestId())) {
                    continue;
                }

                couponIssueService.processIssue(msg);
                metricsService.markHandled(msg.requestId(), "COUPON_ISSUE_REQUEST");

            } catch (Exception e) {
                log.warn("coupon-issue-requests 처리 실패: offset={}", record.offset(), e);
            }
        }
        ack.acknowledge();
    }
}
