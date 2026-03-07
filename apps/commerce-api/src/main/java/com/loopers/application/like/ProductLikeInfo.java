package com.loopers.application.like;

public record ProductLikeInfo(
    Long productId,
    long likeCount,
    boolean liked
) {
    public static ProductLikeInfo of(Long productId, long likeCount, boolean liked) {
        return new ProductLikeInfo(productId, likeCount, liked);
    }
}
