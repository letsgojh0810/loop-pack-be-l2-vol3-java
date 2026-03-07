package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;

public record BrandInfo(
    Long brandId,
    String name,
    String description,
    String imageUrl
) {
    public static BrandInfo from(Brand brand) {
        return new BrandInfo(
            brand.getId(),
            brand.getName(),
            brand.getDescription(),
            brand.getImageUrl()
        );
    }
}
