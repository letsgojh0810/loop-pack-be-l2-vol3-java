package com.loopers.interfaces.api.brand;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Brand Admin", description = "브랜드 어드민 API")
public interface BrandAdminV1ApiSpec {

    @Operation(summary = "브랜드 목록 조회", description = "모든 브랜드 목록을 조회합니다.")
    ApiResponse<BrandAdminV1Dto.BrandListResponse> getAllBrands(
        @Parameter(description = "어드민 LDAP", required = true) String ldap
    );

    @Operation(summary = "브랜드 상세 조회", description = "브랜드 ID로 브랜드 정보를 조회합니다.")
    ApiResponse<BrandAdminV1Dto.BrandResponse> getBrand(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "브랜드 ID", required = true) Long brandId
    );

    @Operation(summary = "브랜드 등록", description = "새로운 브랜드를 등록합니다.")
    ApiResponse<BrandAdminV1Dto.BrandResponse> registerBrand(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        BrandAdminV1Dto.RegisterRequest request
    );

    @Operation(summary = "브랜드 수정", description = "브랜드 정보를 수정합니다.")
    ApiResponse<BrandAdminV1Dto.BrandResponse> updateBrand(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "브랜드 ID", required = true) Long brandId,
        BrandAdminV1Dto.UpdateRequest request
    );

    @Operation(summary = "브랜드 삭제", description = "브랜드를 삭제합니다.")
    ApiResponse<Void> deleteBrand(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "브랜드 ID", required = true) Long brandId
    );
}
