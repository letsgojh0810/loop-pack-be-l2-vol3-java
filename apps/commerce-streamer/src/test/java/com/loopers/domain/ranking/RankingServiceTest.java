package com.loopers.domain.ranking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RankingServiceTest {

    private RankingService rankingService;
    private FakeRankingRepository fakeRepo;

    @BeforeEach
    void setUp() {
        fakeRepo = new FakeRankingRepository();
        rankingService = new RankingService(fakeRepo);
    }

    @DisplayName("가중치 적용 순서 검증")
    @Nested
    class WeightOrder {

        @DisplayName("주문 1건(10,000원)은 좋아요 3건보다 높은 점수를 가진다.")
        @Test
        void orderScoreHigherThanMultipleLikes() {
            // arrange
            String date = "20260410";
            Long productA = 1L; // 좋아요 3건
            Long productB = 2L; // 주문 1건 10,000원

            // act
            rankingService.addLikeScore(date, productA, 1);
            rankingService.addLikeScore(date, productA, 1);
            rankingService.addLikeScore(date, productA, 1);
            rankingService.addOrderScore(date, productB, 1, 10_000);

            // assert
            // A: 0.2 * 3 = 0.6, B: 0.7 * 10000 * 1 = 7000
            List<Long> top = fakeRepo.getTopProductIds(date, 2);
            assertThat(top.get(0)).isEqualTo(productB);
            assertThat(top.get(1)).isEqualTo(productA);
        }

        @DisplayName("조회, 좋아요, 주문 혼합 시 가중치 합산이 정확하다.")
        @Test
        void mixedEventsScoreCalculation() {
            // arrange
            String date = "20260410";
            Long productId = 1L;

            // act
            rankingService.addViewScore(date, productId);    // 0.1
            rankingService.addLikeScore(date, productId, 1); // 0.2
            rankingService.addOrderScore(date, productId, 2, 100); // 0.7 * 100 * 2 = 140

            // assert: 0.1 + 0.2 + 140 = 140.3
            Double score = fakeRepo.getScore(date, productId);
            assertThat(score).isEqualTo(0.1 + 0.2 + 140.0);
        }
    }

    @DisplayName("날짜별 키 분리")
    @Nested
    class DateIsolation {

        @DisplayName("서로 다른 날짜에 쌓인 점수는 독립적으로 조회된다.")
        @Test
        void scoresAreIsolatedByDate() {
            // arrange
            Long productId = 1L;
            rankingService.addViewScore("20260409", productId);
            rankingService.addOrderScore("20260410", productId, 1, 1000);

            // assert
            assertThat(fakeRepo.getScore("20260409", productId)).isEqualTo(0.1);
            assertThat(fakeRepo.getScore("20260410", productId)).isEqualTo(700.0);
        }

        @DisplayName("오늘 데이터가 없는 날짜도 조회 가능하다.")
        @Test
        void canQueryPastDateRanking() {
            // arrange
            rankingService.addOrderScore("20260409", 1L, 3, 5000);
            rankingService.addOrderScore("20260409", 2L, 1, 3000);

            // act
            List<Long> top = fakeRepo.getTopProductIds("20260409", 2);

            // assert: 1L = 0.7*5000*3=10500, 2L = 0.7*3000*1=2100
            assertThat(top.get(0)).isEqualTo(1L);
            assertThat(top.get(1)).isEqualTo(2L);
        }
    }

    @DisplayName("콜드 스타트 Carry-Over")
    @Nested
    class CarryOver {

        @DisplayName("carry-over 후 전날 점수의 10%가 다음날 키에 반영된다.")
        @Test
        void carryOverAppliesTenPercentOfPreviousDay() {
            // arrange
            String today = "20260410";
            String tomorrow = "20260411";
            rankingService.addOrderScore(today, 1L, 1, 10_000); // score = 7000

            // act
            rankingService.carryOver(today, tomorrow);

            // assert: 7000 * 0.1 = 700
            assertThat(fakeRepo.getScore(tomorrow, 1L)).isEqualTo(700.0);
        }

        @DisplayName("carry-over 후 오늘 점수가 쌓이면 내일 랭킹에 합산된다.")
        @Test
        void todayScoreAddsOnTopOfCarryOver() {
            // arrange
            String today = "20260410";
            String tomorrow = "20260411";
            rankingService.addOrderScore(today, 1L, 1, 10_000); // 7000
            rankingService.carryOver(today, tomorrow); // 700 이월

            // act: 내일 이벤트 추가
            rankingService.addViewScore(tomorrow, 1L); // +0.1

            // assert: 700 + 0.1 = 700.1
            assertThat(fakeRepo.getScore(tomorrow, 1L)).isEqualTo(700.1);
        }
    }
}
