package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandInfo;

public class BrandV1Dto {

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
}
