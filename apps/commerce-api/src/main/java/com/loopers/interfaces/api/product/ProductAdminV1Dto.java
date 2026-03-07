package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ProductAdminV1Dto {

    public record RegisterRequest(
        @NotNull(message = "브랜드 ID는 필수입니다.")
        Long brandId,

        @NotBlank(message = "상품명은 필수입니다.")
        String name,

        String description,

        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        int price,

        @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
        int stock,

        String imageUrl
    ) {}

    public record UpdateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        String name,

        String description,

        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        int price,

        @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
        int stock,

        String imageUrl
    ) {}

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
