package com.loopers.interfaces.scheduler;

import com.loopers.domain.outbox.OutboxEvent;
import com.loopers.domain.outbox.OutboxEventRepository;
import com.loopers.domain.outbox.OutboxEventService;
import com.loopers.domain.outbox.OutboxStatus;
import com.loopers.utils.DatabaseCleanUp;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {"catalog-events", "catalog-events-dlq", "order-events", "order-events-dlq"},
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext
class OutboxSchedulerIntegrationTest {

    @Autowired
    private OutboxScheduler outboxScheduler;

    @Autowired
    private OutboxEventService outboxEventService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private KafkaConsumer<String, String> createConsumer(String groupId, String... topics) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(groupId, "true", embeddedKafkaBroker);
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", StringDeserializer.class);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        embeddedKafkaBroker.consumeFromEmbeddedTopics(consumer, topics);
        return consumer;
    }

    @DisplayName("PENDING 상태 Outbox 이벤트가 Kafka로 발행된다.")
    @Nested
    class PublishPendingEvents {

        @DisplayName("PENDING 이벤트가 Kafka 토픽으로 발행되고, PUBLISHED 상태로 변경된다.")
        @Test
        void publishesPendingEvent_andMarksAsPublished() {
            // arrange
            KafkaConsumer<String, String> consumer = createConsumer("test-publish-group", "catalog-events");

            Long savedId = transactionTemplate.execute(status ->
                outboxEventService.save("event-001", "catalog-events", "product-1", "{\"type\":\"LIKE_CREATED\"}")
            );

            // act
            outboxScheduler.publishPendingEvents();

            // assert - Kafka에 메시지가 도착했는지 확인
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
            assertThat(records.count()).isEqualTo(1);

            ConsumerRecord<String, String> record = records.iterator().next();
            assertThat(record.key()).isEqualTo("product-1");
            assertThat(record.value()).contains("LIKE_CREATED");

            // DB 상태 확인
            OutboxEvent saved = outboxEventRepository.findById(savedId).orElseThrow();
            assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);

            consumer.close();
        }

        @DisplayName("재시도 횟수가 MAX에 도달하면 FAILED 상태로 변경된다.")
        @Test
        void marksAsFailed_whenMaxRetryExceeded() {
            // arrange - retryCount=2인 이벤트 저장 (다음 실패 시 MAX_RETRY=3 초과)
            Long savedId = transactionTemplate.execute(status -> {
                OutboxEvent event = OutboxEvent.create(
                    "event-002", "catalog-events", "product-2", "{\"type\":\"LIKE_CREATED\"}"
                );
                event.incrementRetry();
                event.incrementRetry();
                return outboxEventRepository.save(event).getId();
            });

            // retryCount가 2인 상태에서 incrementRetryAndCheckIfFailed 호출 → 3 초과 → FAILED
            boolean isFailed = transactionTemplate.execute(status ->
                outboxEventService.incrementRetryAndCheckIfFailed(savedId)
            );

            // assert
            assertThat(isFailed).isTrue();

            OutboxEvent event = outboxEventRepository.findById(savedId).orElseThrow();
            assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
            assertThat(event.isMaxRetryExceeded()).isTrue();
        }

        @DisplayName("PUBLISHED 상태 이벤트는 재발행되지 않는다.")
        @Test
        void doesNotRepublish_alreadyPublishedEvents() {
            // arrange
            KafkaConsumer<String, String> consumer = createConsumer("test-noreplay-group", "catalog-events");

            transactionTemplate.execute(status -> {
                OutboxEvent event = OutboxEvent.create(
                    "event-003", "catalog-events", "product-3", "{\"type\":\"LIKE_CREATED\"}"
                );
                event.markPublished();
                return outboxEventRepository.save(event);
            });

            // act
            outboxScheduler.publishPendingEvents();

            // assert - PUBLISHED 이므로 getPendingEvents에 포함되지 않음
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
            assertThat(records.count()).isEqualTo(0);

            consumer.close();
        }
    }
}
