package com.loopers.domain.order;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FakeOrderRepository implements OrderRepository {

    private final Map<Long, Order> store = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Order save(Order order) {
        if (order.getId() == null || order.getId() == 0L) {
            ReflectionTestUtils.setField(order, "id", ++sequence);
        }
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findAllByUserId(Long userId) {
        return store.values().stream()
            .filter(order -> order.getUserId().equals(userId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(store.values());
    }
}
