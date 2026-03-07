package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponInfo;
import com.loopers.application.coupon.UserCouponInfo;
import com.loopers.domain.coupon.CouponStatus;
import com.loopers.domain.coupon.CouponType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class CouponAdminV1Dto {

    public record RegisterRequest(
        @NotBlank(message = "쿠폰 이름은 필수입니다.")
        String name,

        @NotNull(message = "쿠폰 타입은 필수입니다.")
        CouponType type,

        @Min(value = 1, message = "할인 값은 1 이상이어야 합니다.")
        int value,

        Integer minOrderAmount,

        @Min(value = 1, message = "유효 기간은 1 이상이어야 합니다.")
        int validDays
    ) {}

    public record UpdateRequest(
        @NotBlank(message = "쿠폰 이름은 필수입니다.")
        String name,

        Integer minOrderAmount
    ) {}

    public record CouponResponse(
        Long couponId,
        String name,
        CouponType type,
        int value,
        Integer minOrderAmount,
        int validDays
    ) {
        public static CouponResponse from(CouponInfo info) {
            return new CouponResponse(
                info.couponId(),
                info.name(),
                info.type(),
                info.value(),
                info.minOrderAmount(),
                info.validDays()
            );
        }
    }

    public record CouponListResponse(
        List<CouponResponse> coupons
    ) {
        public static CouponListResponse from(List<CouponInfo> infos) {
            return new CouponListResponse(
                infos.stream()
                    .map(CouponResponse::from)
                    .toList()
            );
        }
    }

    public record IssuedCouponResponse(
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
        public static IssuedCouponResponse from(UserCouponInfo info) {
            return new IssuedCouponResponse(
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

    public record IssuedCouponListResponse(
        List<IssuedCouponResponse> issuedCoupons
    ) {
        public static IssuedCouponListResponse from(List<UserCouponInfo> infos) {
            return new IssuedCouponListResponse(
                infos.stream()
                    .map(IssuedCouponResponse::from)
                    .toList()
            );
        }
    }
}
