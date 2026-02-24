package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;

public record ProductInfo(
    Long productId,
    String productName,
    String description,
    int price,
    int stock,
    String imageUrl,
    Long brandId,
    String brandName,
    long likeCount,
    boolean liked
) {
    public static ProductInfo of(Product product, Brand brand, long likeCount, boolean liked) {
        return new ProductInfo(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getImageUrl(),
            brand.getId(),
            brand.getName(),
            likeCount,
            liked
        );
    }
}
