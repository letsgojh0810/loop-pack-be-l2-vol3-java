package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface ProductLikeRepository {
    ProductLike save(ProductLike productLike);
    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserIdAndProductId(Long userId, Long productId);
    long countByProductId(Long productId);
    List<ProductLike> findAllByUserId(Long userId);
}
