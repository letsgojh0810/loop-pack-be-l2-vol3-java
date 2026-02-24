package com.loopers.domain.order;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FakeOrderItemRepository implements OrderItemRepository {

    private final Map<Long, OrderItem> store = new HashMap<>();
    private long sequence = 0L;

    @Override
    public List<OrderItem> saveAll(List<OrderItem> items) {
        for (OrderItem item : items) {
            Long currentId = item.getId();
            if (currentId == null || currentId == 0L) {
                ReflectionTestUtils.setField(item, "id", ++sequence);
            }
            store.put(item.getId(), item);
        }
        return items;
    }

    @Override
    public List<OrderItem> findAllByOrderId(Long orderId) {
        return store.values().stream()
            .filter(item -> orderId.equals(item.getOrderId()))
            .collect(Collectors.toList());
    }
}
