package com.loopers.domain.queue;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class QueueService {

    private final QueueRepository queueRepository;

    public long enter(Long userId) {
        Long rank = queueRepository.getRank(userId);
        if (rank != null) return rank + 1;

        double score = System.currentTimeMillis();
        queueRepository.enter(userId, score);
        Long newRank = queueRepository.getRank(userId);
        return newRank != null ? newRank + 1 : 1;
    }

    public Long getPosition(Long userId) {
        Long rank = queueRepository.getRank(userId);
        return rank != null ? rank + 1 : null;
    }

    public long getWaitingCount() {
        return queueRepository.getSize();
    }

    public long estimateWaitSeconds(Long userId) {
        Long position = getPosition(userId);
        if (position == null) return 0;
        long throughputPerSecond = 50;
        return position / throughputPerSecond;
    }

    public List<Long> issueTokens(int count) {
        List<Long> userIds = queueRepository.popMin(count);
        for (Long userId : userIds) {
            String token = UUID.randomUUID().toString();
            queueRepository.saveToken(userId, token, 300);
            queueRepository.saveHardToken(userId, 600);
        }
        return userIds;
    }

    public void validateToken(Long userId, String token) {
        if (!queueRepository.isEnabled()) return;

        String savedToken = queueRepository.getToken(userId);
        if (savedToken == null) {
            throw new CoreException(ErrorType.FORBIDDEN, "입장 토큰이 만료되었거나 존재하지 않습니다.");
        }
        if (!savedToken.equals(token)) {
            throw new CoreException(ErrorType.FORBIDDEN, "유효하지 않은 입장 토큰입니다.");
        }
    }

    public void deleteToken(Long userId) {
        queueRepository.deleteToken(userId);
    }

    public int getBatchSize() {
        long waitingCount = getWaitingCount();
        return waitingCount >= 1000 ? 200 : 50;
    }

    public boolean isEnabled() {
        return queueRepository.isEnabled();
    }

    public String getToken(Long userId) {
        return queueRepository.getToken(userId);
    }
}
