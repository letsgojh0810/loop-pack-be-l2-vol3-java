package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponType;

public record CouponInfo(
    Long couponId,
    String name,
    CouponType type,
    int value,
    Integer minOrderAmount,
    int validDays,
    Integer totalLimit
) {
    public static CouponInfo from(Coupon coupon) {
        return new CouponInfo(
            coupon.getId(),
            coupon.getName(),
            coupon.getType(),
            coupon.getValue(),
            coupon.getMinOrderAmount(),
            coupon.getValidDays(),
            coupon.getTotalLimit()
        );
    }
}
