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

    @DisplayName("순위 조회")
    @Nested
    class GetRank {

        @DisplayName("랭킹에 있는 상품은 1-indexed 순위를 반환한다.")
        @Test
        void returnsOneIndexedRank() {
            fakeRepo.setScore("20260410", 1L, 100.0);
            fakeRepo.setScore("20260410", 2L, 200.0);
            fakeRepo.setScore("20260410", 3L, 50.0);

            assertThat(rankingService.getRank("20260410", 1L)).isEqualTo(2L);
            assertThat(rankingService.getRank("20260410", 2L)).isEqualTo(1L);
            assertThat(rankingService.getRank("20260410", 3L)).isEqualTo(3L);
        }

        @DisplayName("랭킹에 없는 상품은 null을 반환한다.")
        @Test
        void returnsNullWhenNotRanked() {
            fakeRepo.setScore("20260410", 1L, 100.0);

            assertThat(rankingService.getRank("20260410", 999L)).isNull();
        }

        @DisplayName("데이터가 없는 날짜는 null을 반환한다.")
        @Test
        void returnsNullForEmptyDate() {
            assertThat(rankingService.getRank("20260401", 1L)).isNull();
        }
    }

    @DisplayName("Top-N 조회")
    @Nested
    class GetTopProductIds {

        @DisplayName("점수 내림차순으로 상품 ID 목록을 반환한다.")
        @Test
        void returnsTopNInDescOrder() {
            fakeRepo.setScore("20260410", 1L, 300.0);
            fakeRepo.setScore("20260410", 2L, 100.0);
            fakeRepo.setScore("20260410", 3L, 200.0);

            List<Long> top = rankingService.getTopProductIds("20260410", 0, 3);
            assertThat(top).containsExactly(1L, 3L, 2L);
        }

        @DisplayName("이전 날짜 랭킹도 정상 조회된다.")
        @Test
        void canQueryPreviousDateRanking() {
            fakeRepo.setScore("20260409", 10L, 500.0);
            fakeRepo.setScore("20260409", 20L, 300.0);

            List<Long> top = rankingService.getTopProductIds("20260409", 0, 2);
            assertThat(top).containsExactly(10L, 20L);
        }

        @DisplayName("page 파라미터로 페이징이 적용된다.")
        @Test
        void paginationWorks() {
            fakeRepo.setScore("20260410", 1L, 500.0);
            fakeRepo.setScore("20260410", 2L, 400.0);
            fakeRepo.setScore("20260410", 3L, 300.0);
            fakeRepo.setScore("20260410", 4L, 200.0);

            List<Long> page1 = rankingService.getTopProductIds("20260410", 0, 2);
            List<Long> page2 = rankingService.getTopProductIds("20260410", 1, 2);

            assertThat(page1).containsExactly(1L, 2L);
            assertThat(page2).containsExactly(3L, 4L);
        }
    }
}
