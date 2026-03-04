package com.loopers.domain.coupon;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeCouponRepository implements CouponRepository {

    private final Map<Long, Coupon> store = new HashMap<>();
    private Long sequence = 1L;

    @Override
    public Coupon save(Coupon coupon) {
        if (coupon.getId() == null || coupon.getId() == 0L) {
            ReflectionTestUtils.setField(coupon, "id", sequence++);
        }
        store.put(coupon.getId(), coupon);
        return coupon;
    }

    @Override
    public Optional<Coupon> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Coupon> findAll() {
        return new ArrayList<>(store.values());
    }
}
