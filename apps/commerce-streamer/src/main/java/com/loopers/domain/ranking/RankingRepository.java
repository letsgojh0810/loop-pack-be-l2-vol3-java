package com.loopers.domain.ranking;

public interface RankingRepository {
    void incrementScore(String date, Long productId, double score);
    void carryOver(String fromDate, String toDate, double weight);
}
