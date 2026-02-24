package com.loopers.interfaces.api.like;

import com.loopers.application.like.ProductLikeInfo;
import com.loopers.application.product.ProductInfo;

import java.util.List;

public class ProductLikeV1Dto {

    public record LikeResponse(
        Long productId,
        long likeCount,
        boolean liked
    ) {
        public static LikeResponse from(ProductLikeInfo info) {
            return new LikeResponse(
                info.productId(),
                info.likeCount(),
                info.liked()
            );
        }
    }

    public record LikedProductResponse(
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
        public static LikedProductResponse from(ProductInfo info) {
            return new LikedProductResponse(
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

    public record LikedProductListResponse(
        List<LikedProductResponse> products
    ) {
        public static LikedProductListResponse from(List<ProductInfo> infos) {
            return new LikedProductListResponse(
                infos.stream()
                    .map(LikedProductResponse::from)
                    .toList()
            );
        }
    }
}
