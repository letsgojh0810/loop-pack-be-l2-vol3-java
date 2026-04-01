package com.loopers.domain.queue;

import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QueueConcurrencyTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @AfterEach
    void tearDown() {
        redisCleanUp.truncateAll();
    }

    @DisplayName("여러 유저가 동시에 대기열에 진입해도 순번이 중복 없이 정확히 부여된다.")
    @Test
    void assignsUniquePositions_whenMultipleUserEnterConcurrently() throws InterruptedException {
        // arrange
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Long> positions = Collections.synchronizedList(new ArrayList<>());

        // act - 100명 동시 진입
        for (long userId = 1; userId <= threadCount; userId++) {
            long finalUserId = userId;
            executor.submit(() -> {
                try {
                    long position = queueService.enter(finalUserId);
                    positions.add(position);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // assert - 100명 모두 대기열에 등록, 중복 순번 없음
        assertThat(queueService.getWaitingCount()).isEqualTo(threadCount);
        assertThat(positions).hasSize(threadCount);
        long distinctCount = positions.stream().distinct().count();
        assertThat(distinctCount).isEqualTo(threadCount);
    }

    @DisplayName("스케줄러 배치 크기 이상의 요청이 들어와도 배치만큼만 처리된다.")
    @Test
    void processesOnlyBatchSize_whenQueueExceedsCapacity() throws InterruptedException {
        // arrange - 200명 대기열 진입
        int totalUsers = 200;
        for (long userId = 1; userId <= totalUsers; userId++) {
            queueService.enter(userId);
        }
        assertThat(queueService.getWaitingCount()).isEqualTo(totalUsers);

        // act - 배치 크기 50으로 토큰 발급
        int batchSize = 50;
        List<Long> issued = queueService.issueTokens(batchSize);

        // assert - 50명만 처리, 나머지 150명은 대기 유지
        assertThat(issued).hasSize(batchSize);
        assertThat(queueService.getWaitingCount()).isEqualTo(totalUsers - batchSize);

        // 토큰 발급된 유저는 대기열에서 제거됨
        for (Long userId : issued) {
            assertThat(queueService.getPosition(userId)).isNull();
        }
    }
}
