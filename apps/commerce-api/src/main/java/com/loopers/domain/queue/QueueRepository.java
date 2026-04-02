package com.loopers.domain.queue;

import java.util.List;

public interface QueueRepository {
    void enter(Long userId, double score);
    Long getRank(Long userId);
    long getSize();
    List<Long> popMin(int count);
    boolean hasToken(Long userId);
    void saveToken(Long userId, String token, long ttlSeconds);
    void saveHardToken(Long userId, long ttlSeconds);
    String getToken(Long userId);
    void deleteToken(Long userId);
    boolean isEnabled();
}
