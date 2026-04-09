package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class RankingRepositoryImpl implements RankingRepository {

    private static final String KEY_PREFIX = "ranking:all:";
    private static final long TTL_DAYS = 2;
    private static final long TTL_SECONDS = TTL_DAYS * 24 * 60 * 60;

    // ZUNIONSTORE equivalent: copy fromKey * weight into toKey
    private static final RedisScript<Long> CARRY_OVER_SCRIPT = RedisScript.of(
        "local members = redis.call('ZRANGEBYSCORE', KEYS[1], '-inf', '+inf', 'WITHSCORES') " +
        "for i = 1, #members, 2 do " +
        "  redis.call('ZINCRBY', KEYS[2], tonumber(members[i+1]) * tonumber(ARGV[1]), members[i]) " +
        "end " +
        "redis.call('EXPIRE', KEYS[2], tonumber(ARGV[2])) " +
        "return 1",
        Long.class
    );

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void incrementScore(String date, Long productId, double score) {
        String key = KEY_PREFIX + date;
        redisTemplate.opsForZSet().incrementScore(key, productId.toString(), score);
        redisTemplate.expire(key, TTL_DAYS, TimeUnit.DAYS);
    }

    @Override
    public void carryOver(String fromDate, String toDate, double weight) {
        String fromKey = KEY_PREFIX + fromDate;
        String toKey = KEY_PREFIX + toDate;
        redisTemplate.execute(CARRY_OVER_SCRIPT, List.of(fromKey, toKey),
                String.valueOf(weight), String.valueOf(TTL_SECONDS));
    }
}
