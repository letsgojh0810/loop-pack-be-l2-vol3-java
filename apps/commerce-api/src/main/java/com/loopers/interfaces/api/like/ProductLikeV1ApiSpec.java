package com.loopers.interfaces.api.like;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "ProductLike", description = "상품 좋아요 API")
public interface ProductLikeV1ApiSpec {

    @Operation(summary = "상품 좋아요", description = "상품에 좋아요를 추가합니다.")
    ApiResponse<ProductLikeV1Dto.LikeResponse> like(
        @Parameter(description = "로그인 ID", required = true) String loginId,
        @Parameter(description = "비밀번호", required = true) String password,
        @Parameter(description = "상품 ID", required = true) Long productId
    );

    @Operation(summary = "상품 좋아요 취소", description = "상품에 좋아요를 취소합니다.")
    ApiResponse<ProductLikeV1Dto.LikeResponse> unlike(
        @Parameter(description = "로그인 ID", required = true) String loginId,
        @Parameter(description = "비밀번호", required = true) String password,
        @Parameter(description = "상품 ID", required = true) Long productId
    );

    @Operation(summary = "좋아요한 상품 목록 조회", description = "사용자가 좋아요한 상품 목록을 조회합니다.")
    ApiResponse<ProductLikeV1Dto.LikedProductListResponse> getLikedProducts(
        @Parameter(description = "로그인 ID", required = true) String loginId,
        @Parameter(description = "비밀번호", required = true) String password,
        @Parameter(description = "사용자 ID", required = true) Long userId
    );
}
