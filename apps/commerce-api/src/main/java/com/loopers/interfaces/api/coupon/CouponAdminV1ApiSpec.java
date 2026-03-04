package com.loopers.interfaces.api.coupon;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Coupon Admin", description = "쿠폰 어드민 API")
public interface CouponAdminV1ApiSpec {

    @Operation(summary = "쿠폰 템플릿 목록 조회", description = "모든 쿠폰 템플릿 목록을 조회합니다.")
    ApiResponse<CouponAdminV1Dto.CouponListResponse> getAllCoupons(
        @Parameter(description = "어드민 LDAP", required = true) String ldap
    );

    @Operation(summary = "쿠폰 템플릿 상세 조회", description = "쿠폰 ID로 쿠폰 템플릿 정보를 조회합니다.")
    ApiResponse<CouponAdminV1Dto.CouponResponse> getCoupon(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "쿠폰 ID", required = true) Long couponId
    );

    @Operation(summary = "쿠폰 템플릿 등록", description = "새로운 쿠폰 템플릿을 등록합니다.")
    ApiResponse<CouponAdminV1Dto.CouponResponse> registerCoupon(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        CouponAdminV1Dto.RegisterRequest request
    );

    @Operation(summary = "쿠폰 템플릿 수정", description = "쿠폰 이름과 최소 주문금액을 수정합니다.")
    ApiResponse<CouponAdminV1Dto.CouponResponse> updateCoupon(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "쿠폰 ID", required = true) Long couponId,
        CouponAdminV1Dto.UpdateRequest request
    );

    @Operation(summary = "쿠폰 템플릿 삭제", description = "쿠폰 템플릿을 삭제합니다.")
    ApiResponse<Void> deleteCoupon(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "쿠폰 ID", required = true) Long couponId
    );

    @Operation(summary = "쿠폰 발급 내역 조회", description = "쿠폰 ID로 발급 내역을 조회합니다.")
    ApiResponse<CouponAdminV1Dto.IssuedCouponListResponse> getIssuedCoupons(
        @Parameter(description = "어드민 LDAP", required = true) String ldap,
        @Parameter(description = "쿠폰 ID", required = true) Long couponId
    );
}
