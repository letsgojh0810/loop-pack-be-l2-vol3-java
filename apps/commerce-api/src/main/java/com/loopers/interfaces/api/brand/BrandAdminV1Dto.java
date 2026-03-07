package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandInfo;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class BrandAdminV1Dto {

    public record RegisterRequest(
        @NotBlank(message = "브랜드명은 필수입니다.")
        String name,

        String description,

        String imageUrl
    ) {}

    public record UpdateRequest(
        @NotBlank(message = "브랜드명은 필수입니다.")
        String name,

        String description,

        String imageUrl
    ) {}

    public record BrandResponse(
        Long brandId,
        String name,
        String description,
        String imageUrl
    ) {
        public static BrandResponse from(BrandInfo info) {
            return new BrandResponse(
                info.brandId(),
                info.name(),
                info.description(),
                info.imageUrl()
            );
        }
    }

    public record BrandListResponse(
        List<BrandResponse> brands
    ) {
        public static BrandListResponse from(List<BrandInfo> infos) {
            return new BrandListResponse(
                infos.stream()
                    .map(BrandResponse::from)
                    .toList()
            );
        }
    }
}
