package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RankingService {

    private static final double WEIGHT_VIEW = 0.1;
    private static final double WEIGHT_LIKE = 0.2;
    private static final double WEIGHT_ORDER = 0.7;

    private final RankingRepository rankingRepository;

    public void addViewScore(String date, Long productId) {
        rankingRepository.incrementScore(date, productId, WEIGHT_VIEW);
    }

    public void addLikeScore(String date, Long productId, int delta) {
        rankingRepository.incrementScore(date, productId, WEIGHT_LIKE * delta);
    }

    public void addOrderScore(String date, Long productId, int quantity, int price) {
        rankingRepository.incrementScore(date, productId, WEIGHT_ORDER * price * quantity);
    }

    public void carryOver(String fromDate, String toDate) {
        rankingRepository.carryOver(fromDate, toDate, 0.1);
    }
}
