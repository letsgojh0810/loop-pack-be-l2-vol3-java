package com.loopers.infrastructure.queue;

import com.loopers.domain.queue.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class QueueRepositoryImpl implements QueueRepository {

    private static final String WAITING_KEY = "queue:waiting";
    private static final String TOKEN_KEY_PREFIX = "queue:token:";
    private static final String HARD_TOKEN_KEY_PREFIX = "queue:token:hard:";
    private static final String ENABLED_KEY = "queue:enabled";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void enter(Long userId, double score) {
        redisTemplate.opsForZSet().add(WAITING_KEY, userId.toString(), score);
    }

    @Override
    public Long getRank(Long userId) {
        return redisTemplate.opsForZSet().rank(WAITING_KEY, userId.toString());
    }

    @Override
    public long getSize() {
        Long size = redisTemplate.opsForZSet().size(WAITING_KEY);
        return size != null ? size : 0;
    }

    @Override
    public List<Long> popMin(int count) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
            redisTemplate.opsForZSet().popMin(WAITING_KEY, count);
        if (tuples == null || tuples.isEmpty()) return Collections.emptyList();
        return tuples.stream()
            .map(ZSetOperations.TypedTuple::getValue)
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }

    @Override
    public boolean hasToken(Long userId) {
        Boolean exists = redisTemplate.hasKey(TOKEN_KEY_PREFIX + userId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void saveToken(Long userId, String token, long ttlSeconds) {
        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + userId, token, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void saveHardToken(Long userId, long ttlSeconds) {
        redisTemplate.opsForValue().set(HARD_TOKEN_KEY_PREFIX + userId, "1", ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String getToken(Long userId) {
        // Hard TTL 만료 시 토큰 무효화
        Boolean hardExists = redisTemplate.hasKey(HARD_TOKEN_KEY_PREFIX + userId);
        if (!Boolean.TRUE.equals(hardExists)) {
            redisTemplate.delete(TOKEN_KEY_PREFIX + userId);
            return null;
        }
        return redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + userId);
    }

    @Override
    public void deleteToken(Long userId) {
        redisTemplate.delete(TOKEN_KEY_PREFIX + userId);
        redisTemplate.delete(HARD_TOKEN_KEY_PREFIX + userId);
    }

    @Override
    public boolean isEnabled() {
        String value = redisTemplate.opsForValue().get(ENABLED_KEY);
        // key 없으면 기본 활성화
        if (value == null) return true;
        return "true".equalsIgnoreCase(value);
    }
}
