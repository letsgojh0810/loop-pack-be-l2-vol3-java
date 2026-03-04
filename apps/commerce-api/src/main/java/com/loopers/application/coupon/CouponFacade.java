package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.coupon.UserCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CouponFacade {

    private final CouponService couponService;

    public UserCouponInfo issueCoupon(Long couponId, Long userId) {
        UserCoupon userCoupon = couponService.issueCoupon(couponId, userId);
        Coupon coupon = couponService.getCoupon(userCoupon.getCouponId());
        return UserCouponInfo.from(userCoupon, coupon);
    }

    public List<UserCouponInfo> getMyUserCoupons(Long userId) {
        List<UserCoupon> userCoupons = couponService.getUserCoupons(userId);
        return userCoupons.stream()
            .map(userCoupon -> {
                Coupon coupon = couponService.getCoupon(userCoupon.getCouponId());
                return UserCouponInfo.from(userCoupon, coupon);
            })
            .toList();
    }

    public CouponInfo registerCoupon(String name, CouponType type, int value, Integer minOrderAmount, int validDays) {
        Coupon coupon = couponService.registerCoupon(name, type, value, minOrderAmount, validDays);
        return CouponInfo.from(coupon);
    }

    public List<CouponInfo> getAllCoupons() {
        return couponService.getAllCoupons().stream()
            .map(CouponInfo::from)
            .toList();
    }

    public CouponInfo getCoupon(Long couponId) {
        Coupon coupon = couponService.getCoupon(couponId);
        return CouponInfo.from(coupon);
    }

    public List<UserCouponInfo> getIssuedCoupons(Long couponId) {
        List<UserCoupon> userCoupons = couponService.getIssuedCoupons(couponId);
        Coupon coupon = couponService.getCoupon(couponId);
        return userCoupons.stream()
            .map(userCoupon -> UserCouponInfo.from(userCoupon, coupon))
            .toList();
    }

    public CouponInfo updateCoupon(Long couponId, String name, Integer minOrderAmount) {
        Coupon coupon = couponService.updateCoupon(couponId, name, minOrderAmount);
        return CouponInfo.from(coupon);
    }

    public void deleteCoupon(Long couponId) {
        couponService.deleteCoupon(couponId);
    }
}
