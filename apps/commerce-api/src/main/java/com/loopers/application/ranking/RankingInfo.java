package com.loopers.application.ranking;

public record RankingInfo(
        Long rank,
        Long productId,
        String productName,
        String imageUrl,
        int price,
        Long brandId,
        String brandName
) {}
