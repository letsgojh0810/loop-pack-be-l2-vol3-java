package com.loopers.domain.coupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {
    UserCoupon save(UserCoupon userCoupon);
    Optional<UserCoupon> findById(Long id);
    Optional<UserCoupon> findByIdForUpdate(Long id);
    List<UserCoupon> findAllByUserId(Long userId);
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
    List<UserCoupon> findAllByCouponId(Long couponId);
}
