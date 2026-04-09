package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingService;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RankingIntegrationTest {

    private static final String KEY_PREFIX = "ranking:all:";

    @Autowired
    private RankingService rankingService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @AfterEach
    void tearDown() {
        redisCleanUp.truncateAll();
    }

    private void addScore(String date, Long productId, double score) {
        redisTemplate.opsForZSet().add(KEY_PREFIX + date, productId.toString(), score);
    }

    @DisplayName("E2E: ZSET 점수 반영 → API 조회 흐름")
    @Nested
    class E2EFlow {

        @DisplayName("점수를 누적하면 내림차순으로 상위 상품이 반환된다.")
        @Test
        void topRankingsReturnedInDescOrder() {
            // arrange
            String date = "20260410";
            addScore(date, 1L, 7000.0);
            addScore(date, 2L, 0.2);
            addScore(date, 3L, 3500.0);

            // act
            List<Long> top = rankingService.getTopProductIds(date, 0, 3);

            // assert
            assertThat(top).containsExactly(1L, 3L, 2L);
        }

        @DisplayName("이전 날짜 랭킹이 날짜 변경 후에도 조회된다.")
        @Test
        void previousDateRankingAccessibleAfterDateChange() {
            // arrange
            String yesterday = "20260409";
            String today = "20260410";
            addScore(yesterday, 10L, 500.0);
            addScore(today, 20L, 300.0);

            // act & assert
            List<Long> yesterdayTop = rankingService.getTopProductIds(yesterday, 0, 5);
            List<Long> todayTop = rankingService.getTopProductIds(today, 0, 5);

            assertThat(yesterdayTop).containsExactly(10L);
            assertThat(todayTop).containsExactly(20L);
        }

        @DisplayName("가중치 적용 결과 주문 1건이 좋아요 3건보다 높은 순위를 가진다.")
        @Test
        void orderRanksHigherThanMultipleLikes() {
            // arrange: streamer 가중치 계산 결과 시뮬레이션
            String date = "20260410";
            // 상품 A: 좋아요 3건 → 0.2 * 3 = 0.6
            addScore(date, 1L, 0.6);
            // 상품 B: 주문 1건 1만원 → 0.7 * 10000 = 7000
            addScore(date, 2L, 7000.0);

            // act
            Long rankA = rankingService.getRank(date, 1L);
            Long rankB = rankingService.getRank(date, 2L);

            // assert
            assertThat(rankB).isLessThan(rankA);
            assertThat(rankB).isEqualTo(1L);
            assertThat(rankA).isEqualTo(2L);
        }
    }

    @DisplayName("순위가 없는 상품 조회 시 null을 반환한다.")
    @Test
    void returnsNullRankForUnrankedProduct() {
        addScore("20260410", 1L, 100.0);

        Long rank = rankingService.getRank("20260410", 999L);

        assertThat(rank).isNull();
    }
}
