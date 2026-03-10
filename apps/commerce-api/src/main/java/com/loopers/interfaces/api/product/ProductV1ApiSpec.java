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

    @Operation(summary = "상품 목록 조회", description = "상품 목록을 조회합니다. 필터링, 정렬, 페이지네이션을 지원합니다.")
    ApiResponse<ProductV1Dto.ProductListResponse> getProducts(
        @Parameter(description = "브랜드 ID (선택)") Long brandId,
        @Parameter(description = "정렬 기준: latest(최신순), price_asc(가격 오름차순), likes_desc(좋아요 내림차순)") String sort,
        @Parameter(description = "페이지 번호 (0부터 시작)") int page,
        @Parameter(description = "페이지당 상품 수") int size
    );
}
