package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class RankingRepositoryImpl implements RankingRepository {

    private static final String KEY_PREFIX = "ranking:all:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public List<String> getTopProductIds(String date, int offset, int count) {
        Set<String> result = redisTemplate.opsForZSet()
                .reverseRange(KEY_PREFIX + date, offset, (long) offset + count - 1);
        if (result == null || result.isEmpty()) return Collections.emptyList();
        return new ArrayList<>(result);
    }

    @Override
    public Long getRank(String date, Long productId) {
        return redisTemplate.opsForZSet().reverseRank(KEY_PREFIX + date, productId.toString());
    }

    @Override
    public long getTotalCount(String date) {
        Long count = redisTemplate.opsForZSet().size(KEY_PREFIX + date);
        return count != null ? count : 0;
    }
}
