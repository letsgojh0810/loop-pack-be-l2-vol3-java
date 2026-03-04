package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final UserCouponJpaRepository userCouponJpaRepository;

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        return userCouponJpaRepository.save(userCoupon);
    }

    @Override
    public Optional<UserCoupon> findById(Long id) {
        return userCouponJpaRepository.findById(id);
    }

    @Override
    public Optional<UserCoupon> findByIdForUpdate(Long id) {
        return userCouponJpaRepository.findByIdForUpdate(id);
    }

    @Override
    public List<UserCoupon> findAllByUserId(Long userId) {
        return userCouponJpaRepository.findAllByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndCouponId(Long userId, Long couponId) {
        return userCouponJpaRepository.existsByUserIdAndCouponId(userId, couponId);
    }

    @Override
    public List<UserCoupon> findAllByCouponId(Long couponId) {
        return userCouponJpaRepository.findAllByCouponId(couponId);
    }
}
