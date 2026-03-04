package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCouponJpaRepository extends JpaRepository<UserCoupon, Long> {
    List<UserCoupon> findAllByUserId(Long userId);
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
    List<UserCoupon> findAllByCouponId(Long couponId);
}
