package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponStatus;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.coupon.UserCoupon;

import java.time.LocalDateTime;

public record UserCouponInfo(
    Long userCouponId,
    Long couponId,
    Long userId,
    String couponName,
    CouponType type,
    int value,
    CouponStatus status,
    LocalDateTime issuedAt,
    LocalDateTime expiredAt
) {
    public static UserCouponInfo from(UserCoupon userCoupon, Coupon coupon) {
        return new UserCouponInfo(
            userCoupon.getId(),
            userCoupon.getCouponId(),
            userCoupon.getUserId(),
            coupon.getName(),
            coupon.getType(),
            coupon.getValue(),
            userCoupon.getStatus(),
            userCoupon.getIssuedAt(),
            userCoupon.getExpiredAt()
        );
    }
}
