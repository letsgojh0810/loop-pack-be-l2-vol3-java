package com.loopers.application.like;

import com.loopers.application.log.UserActionEvent;
import com.loopers.application.product.ProductInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.ProductLike;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class ProductLikeFacade {

    private final ProductService productService;
    private final ProductLikeService productLikeService;
    private final BrandService brandService;
    private final ApplicationEventPublisher eventPublisher;

    public void like(Long userId, Long productId) {
        productService.getProduct(productId);
        productLikeService.like(userId, productId);
        eventPublisher.publishEvent(new LikeCreatedEvent(productId));
        eventPublisher.publishEvent(new UserActionEvent(userId, "LIKE_PRODUCT", "PRODUCT", productId));
        // 좋아요 수는 변경 빈도가 높아 캐시 무효화 대신 TTL 만료에 맡김 (약간의 stale 허용)
    }

    public void unlike(Long userId, Long productId) {
        productLikeService.unlike(userId, productId);
        eventPublisher.publishEvent(new LikeCancelledEvent(productId));
        // 좋아요 수는 변경 빈도가 높아 캐시 무효화 대신 TTL 만료에 맡김 (약간의 stale 허용)
    }

    public ProductLikeInfo getLikeInfo(Long userId, Long productId) {
        productService.getProduct(productId);
        boolean liked = productLikeService.isLiked(userId, productId);
        long likeCount = productLikeService.getLikeCount(productId);
        return ProductLikeInfo.of(productId, likeCount, liked);
    }

    public List<ProductInfo> getLikedProducts(Long userId) {
        List<ProductLike> likes = productLikeService.getLikedProducts(userId);
        return likes.stream()
            .map(like -> {
                try {
                    Product product = productService.getProduct(like.getProductId());
                    Brand brand = brandService.getBrand(product.getBrandId());
                    return ProductInfo.of(product, brand, product.getLikeCount(), true);
                } catch (CoreException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
