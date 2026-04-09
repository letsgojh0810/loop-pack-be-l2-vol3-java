package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class RankingService {

    private final RankingRepository rankingRepository;

    public List<Long> getTopProductIds(String date, int page, int size) {
        int offset = page * size;
        return rankingRepository.getTopProductIds(date, offset, size)
                .stream().map(Long::parseLong).toList();
    }

    public Long getRank(String date, Long productId) {
        Long rank = rankingRepository.getRank(date, productId);
        return rank != null ? rank + 1 : null;
    }

    public long getTotalCount(String date) {
        return rankingRepository.getTotalCount(date);
    }
}
