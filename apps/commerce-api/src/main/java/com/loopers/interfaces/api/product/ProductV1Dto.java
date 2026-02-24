package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;

import java.util.List;

public class ProductV1Dto {

    public record ProductResponse(
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
        public static ProductResponse from(ProductInfo info) {
            return new ProductResponse(
                info.productId(),
                info.productName(),
                info.description(),
                info.price(),
                info.stock(),
                info.imageUrl(),
                info.brandId(),
                info.brandName(),
                info.likeCount(),
                info.liked()
            );
        }
    }

    public record ProductListResponse(
        List<ProductResponse> products
    ) {
        public static ProductListResponse from(List<ProductInfo> infos) {
            return new ProductListResponse(
                infos.stream()
                    .map(ProductResponse::from)
                    .toList()
            );
        }
    }
}
