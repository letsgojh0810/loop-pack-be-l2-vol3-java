package com.loopers.application.like;

import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductLikeFacade {

    private final ProductService productService;
    private final ProductLikeService productLikeService;

    public void like(Long userId, Long productId) {
        productService.getProduct(productId);
        productLikeService.like(userId, productId);
    }

    public void unlike(Long userId, Long productId) {
        productLikeService.unlike(userId, productId);
    }

    public ProductLikeInfo getLikeInfo(Long userId, Long productId) {
        long likeCount = productLikeService.getLikeCount(productId);
        boolean liked = productLikeService.isLiked(userId, productId);
        return ProductLikeInfo.of(productId, likeCount, liked);
    }
}
