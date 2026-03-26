package com.loopers.domain.like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;

    @Transactional
    public void like(Long userId, Long productId) {
        boolean alreadyLiked = productLikeRepository.findByUserIdAndProductId(userId, productId).isPresent();
        if (alreadyLiked) {
            return;
        }
        ProductLike productLike = new ProductLike(userId, productId);
        productLikeRepository.save(productLike);
    }

    @Transactional
    public void unlike(Long userId, Long productId) {
        productLikeRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public long getLikeCount(Long productId) {
        return productLikeRepository.countByProductId(productId);
    }

    public boolean isLiked(Long userId, Long productId) {
        return productLikeRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }

    public List<ProductLike> getLikedProducts(Long userId) {
        return productLikeRepository.findAllByUserId(userId);
    }
}
