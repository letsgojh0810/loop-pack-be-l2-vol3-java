package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product Admin", description = "상품 어드민 API")
public interface ProductAdminV1ApiSpec {

    @Operation(summary = "상품 목록 조회", description = "모든 상품 목록을 조회합니다.")
    ApiResponse<ProductAdminV1Dto.ProductListResponse> getProducts(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "브랜드 ID (선택)") Long brandId
    );

    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.")
    ApiResponse<ProductAdminV1Dto.ProductResponse> getProductDetail(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "상품 ID", required = true) Long productId
    );

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    ApiResponse<ProductAdminV1Dto.ProductResponse> registerProduct(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        ProductAdminV1Dto.RegisterRequest request
    );

    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다.")
    ApiResponse<ProductAdminV1Dto.ProductResponse> updateProduct(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "상품 ID", required = true) Long productId,
        ProductAdminV1Dto.UpdateRequest request
    );

    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    ApiResponse<Void> deleteProduct(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "상품 ID", required = true) Long productId
    );
}
