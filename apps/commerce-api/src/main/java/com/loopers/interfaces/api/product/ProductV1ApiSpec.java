package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product", description = "상품 API")
public interface ProductV1ApiSpec {

    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.")
    ApiResponse<ProductV1Dto.ProductResponse> getProductDetail(
        @Parameter(description = "상품 ID", required = true) Long productId
    );

    @Operation(summary = "상품 목록 조회", description = "상품 목록을 조회합니다. brandId로 필터링 가능합니다.")
    ApiResponse<ProductV1Dto.ProductListResponse> getProducts(
        @Parameter(description = "브랜드 ID (선택)") Long brandId
    );
}
