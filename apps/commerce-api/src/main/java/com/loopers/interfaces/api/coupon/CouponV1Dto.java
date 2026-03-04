package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.UserCouponInfo;
import com.loopers.domain.coupon.CouponStatus;
import com.loopers.domain.coupon.CouponType;

import java.time.LocalDateTime;
import java.util.List;

public class CouponV1Dto {

    public record UserCouponResponse(
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
        public static UserCouponResponse from(UserCouponInfo info) {
            return new UserCouponResponse(
                info.userCouponId(),
                info.couponId(),
                info.userId(),
                info.couponName(),
                info.type(),
                info.value(),
                info.status(),
                info.issuedAt(),
                info.expiredAt()
            );
        }
    }

    public record UserCouponListResponse(
        List<UserCouponResponse> coupons
    ) {
        public static UserCouponListResponse from(List<UserCouponInfo> infos) {
            return new UserCouponListResponse(
                infos.stream()
                    .map(UserCouponResponse::from)
                    .toList()
            );
        }
    }
}
