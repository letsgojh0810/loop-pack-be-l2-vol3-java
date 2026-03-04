package com.loopers.domain.coupon;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FakeUserCouponRepository implements UserCouponRepository {

    private final Map<Long, UserCoupon> store = new HashMap<>();
    private Long sequence = 1L;

    @Override
    public UserCoupon save(UserCoupon userCoupon) {
        if (userCoupon.getId() == null || userCoupon.getId() == 0L) {
            ReflectionTestUtils.setField(userCoupon, "id", sequence++);
        }
        store.put(userCoupon.getId(), userCoupon);
        return userCoupon;
    }

    @Override
    public Optional<UserCoupon> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<UserCoupon> findAllByUserId(Long userId) {
        return store.values().stream()
            .filter(uc -> uc.getUserId().equals(userId))
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndCouponId(Long userId, Long couponId) {
        return store.values().stream()
            .anyMatch(uc -> uc.getUserId().equals(userId) && uc.getCouponId().equals(couponId));
    }

    @Override
    public List<UserCoupon> findAllByCouponId(Long couponId) {
        return store.values().stream()
            .filter(uc -> uc.getCouponId().equals(couponId))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<UserCoupon> findByIdForUpdate(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}
