package com.loopers.domain.like;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeProductLikeRepository implements ProductLikeRepository {

    private final Map<Long, ProductLike> store = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public ProductLike save(ProductLike productLike) {
        if (productLike.getId() == null) {
            ReflectionTestUtils.setField(productLike, "id", sequence.getAndIncrement());
        }
        store.put(productLike.getId(), productLike);
        return productLike;
    }

    @Override
    public Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId) {
        return store.values().stream()
            .filter(like -> like.getUserId().equals(userId) && like.getProductId().equals(productId))
            .findFirst();
    }

    @Override
    public void deleteByUserIdAndProductId(Long userId, Long productId) {
        store.values().stream()
            .filter(like -> like.getUserId().equals(userId) && like.getProductId().equals(productId))
            .findFirst()
            .ifPresent(like -> store.remove(like.getId()));
    }

    @Override
    public long countByProductId(Long productId) {
        return store.values().stream()
            .filter(like -> like.getProductId().equals(productId))
            .count();
    }
}
