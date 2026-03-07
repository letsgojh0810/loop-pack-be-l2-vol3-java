package com.loopers.application.like;

import com.loopers.application.product.ProductInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.ProductLike;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class ProductLikeFacade {

    private final ProductService productService;
    private final ProductLikeService productLikeService;
    private final BrandService brandService;

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

    public List<ProductInfo> getLikedProducts(Long userId) {
        List<ProductLike> likes = productLikeService.getLikedProducts(userId);
        return likes.stream()
            .map(like -> {
                try {
                    Product product = productService.getProduct(like.getProductId());
                    Brand brand = brandService.getBrand(product.getBrandId());
                    long likeCount = productLikeService.getLikeCount(product.getId());
                    return ProductInfo.of(product, brand, likeCount, true);
                } catch (CoreException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
