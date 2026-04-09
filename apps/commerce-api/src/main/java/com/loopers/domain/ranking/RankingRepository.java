package com.loopers.domain.ranking;

import java.util.List;

public interface RankingRepository {
    List<String> getTopProductIds(String date, int offset, int count);
    Long getRank(String date, Long productId);
    long getTotalCount(String date);
}
