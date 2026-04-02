package com.loopers.domain.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FakeQueueRepository implements QueueRepository {

    // score → userId (TreeMap으로 순서 보장)
    private final TreeMap<Double, Long> waitingQueue = new TreeMap<>();
    private final Map<Long, Double> userScores = new HashMap<>();
    private final Map<Long, String> tokens = new HashMap<>();
    private final Map<Long, Boolean> hardTokens = new HashMap<>();
    private boolean enabled = true;

    @Override
    public void enter(Long userId, double score) {
        waitingQueue.put(score, userId);
        userScores.put(userId, score);
    }

    @Override
    public Long getRank(Long userId) {
        if (!userScores.containsKey(userId)) return null;
        double myScore = userScores.get(userId);
        long rank = 0;
        for (double score : waitingQueue.keySet()) {
            if (score < myScore) rank++;
            else break;
        }
        return rank;
    }

    @Override
    public long getSize() {
        return waitingQueue.size();
    }

    @Override
    public List<Long> popMin(int count) {
        List<Long> result = new ArrayList<>();
        int popped = 0;
        while (!waitingQueue.isEmpty() && popped < count) {
            Map.Entry<Double, Long> entry = waitingQueue.firstEntry();
            waitingQueue.remove(entry.getKey());
            userScores.remove(entry.getValue());
            result.add(entry.getValue());
            popped++;
        }
        return result;
    }

    @Override
    public boolean hasToken(Long userId) {
        return tokens.containsKey(userId);
    }

    @Override
    public void saveToken(Long userId, String token, long ttlSeconds) {
        tokens.put(userId, token);
    }

    @Override
    public void saveHardToken(Long userId, long ttlSeconds) {
        hardTokens.put(userId, true);
    }

    @Override
    public String getToken(Long userId) {
        if (!hardTokens.getOrDefault(userId, false)) {
            tokens.remove(userId);
            return null;
        }
        return tokens.get(userId);
    }

    @Override
    public void deleteToken(Long userId) {
        tokens.remove(userId);
        hardTokens.remove(userId);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // 테스트 헬퍼
    public void expireToken(Long userId) {
        hardTokens.remove(userId);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
