package com.loopers.domain.ranking;

import java.util.*;

public class FakeRankingRepository implements RankingRepository {

    // date → sorted (productId → score), 내림차순 정렬
    private final Map<String, Map<Long, Double>> store = new HashMap<>();

    public void setScore(String date, Long productId, double score) {
        store.computeIfAbsent(date, k -> new HashMap<>()).put(productId, score);
    }

    @Override
    public List<String> getTopProductIds(String date, int offset, int count) {
        Map<Long, Double> scores = store.getOrDefault(date, Map.of());
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .skip(offset)
                .limit(count)
                .map(e -> e.getKey().toString())
                .toList();
    }

    @Override
    public Long getRank(String date, Long productId) {
        Map<Long, Double> scores = store.getOrDefault(date, Map.of());
        if (!scores.containsKey(productId)) return null;
        double myScore = scores.get(productId);
        long rank = scores.values().stream().filter(s -> s > myScore).count();
        return rank;
    }

    @Override
    public long getTotalCount(String date) {
        return store.getOrDefault(date, Map.of()).size();
    }
}
