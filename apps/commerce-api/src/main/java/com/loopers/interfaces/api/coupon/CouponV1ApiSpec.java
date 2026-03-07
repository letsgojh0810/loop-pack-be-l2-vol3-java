package com.loopers.interfaces.api.coupon;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Coupon", description = "쿠폰 API")
public interface CouponV1ApiSpec {

    @Operation(summary = "쿠폰 발급", description = "쿠폰을 발급합니다.")
    ApiResponse<CouponV1Dto.UserCouponResponse> issueCoupon(
        @Parameter(description = "로그인 ID", required = true) String loginId,
        @Parameter(description = "비밀번호", required = true) String password,
        @Parameter(description = "쿠폰 ID", required = true) Long couponId
    );

    @Operation(summary = "내 쿠폰 목록 조회", description = "로그인한 사용자의 쿠폰 목록을 조회합니다.")
    ApiResponse<CouponV1Dto.UserCouponListResponse> getMyUserCoupons(
        @Parameter(description = "로그인 ID", required = true) String loginId,
        @Parameter(description = "비밀번호", required = true) String password
    );
}
