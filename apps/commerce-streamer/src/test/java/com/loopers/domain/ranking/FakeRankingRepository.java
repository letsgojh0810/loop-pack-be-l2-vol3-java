package com.loopers.domain.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeRankingRepository implements RankingRepository {

    // date → (productId → score)
    private final Map<String, Map<Long, Double>> store = new HashMap<>();

    @Override
    public void incrementScore(String date, Long productId, double score) {
        store.computeIfAbsent(date, k -> new HashMap<>())
             .merge(productId, score, Double::sum);
    }

    @Override
    public void carryOver(String fromDate, String toDate, double weight) {
        Map<Long, Double> fromMap = store.getOrDefault(fromDate, Map.of());
        Map<Long, Double> toMap = store.computeIfAbsent(toDate, k -> new HashMap<>());
        fromMap.forEach((productId, score) ->
                toMap.merge(productId, score * weight, Double::sum));
    }

    // 테스트 헬퍼
    public Double getScore(String date, Long productId) {
        return store.getOrDefault(date, Map.of()).get(productId);
    }

    public List<Long> getTopProductIds(String date, int count) {
        return store.getOrDefault(date, Map.of()).entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(count)
                .map(Map.Entry::getKey)
                .toList();
    }
}
