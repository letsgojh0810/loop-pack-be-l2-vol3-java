package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    @Transactional
    public UserCoupon issueCoupon(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new CoreException(ErrorType.CONFLICT, "이미 발급받은 쿠폰입니다.");
        }

        UserCoupon userCoupon = UserCoupon.issue(coupon, userId);
        return userCouponRepository.save(userCoupon);
    }

    @Transactional(readOnly = true)
    public List<UserCoupon> getUserCoupons(Long userId) {
        return userCouponRepository.findAllByUserId(userId);
    }

    @Transactional
    public UserCoupon getValidatedUserCoupon(Long userCouponId, Long userId, int originalAmount) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "발급된 쿠폰을 찾을 수 없습니다."));

        userCoupon.validate(userId);

        Coupon coupon = couponRepository.findById(userCoupon.getCouponId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        coupon.validateMinOrderAmount(originalAmount);

        return userCoupon;
    }

    @Transactional(readOnly = true)
    public Coupon getCoupon(Long couponId) {
        return couponRepository.findById(couponId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Transactional
    public Coupon registerCoupon(String name, CouponType type, int value, Integer minOrderAmount, int validDays) {
        Coupon coupon = new Coupon(name, type, value, minOrderAmount, validDays);
        return couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public List<UserCoupon> getIssuedCoupons(Long couponId) {
        return userCouponRepository.findAllByCouponId(couponId);
    }

    @Transactional
    public Coupon updateCoupon(Long couponId, String name, Integer minOrderAmount) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
        coupon.update(name, minOrderAmount);
        return coupon;
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
        coupon.delete();
    }
}
