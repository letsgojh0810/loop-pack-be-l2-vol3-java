package com.loopers.domain.product;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeProductRepository implements ProductRepository {

    private final Map<Long, Product> store = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Product save(Product product) {
        if (product.getId() == null || product.getId().equals(0L)) {
            sequence++;
            ReflectionTestUtils.setField(product, "id", sequence);
        }
        store.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Product> findAllByBrandId(Long brandId) {
        List<Product> result = new ArrayList<>();
        for (Product product : store.values()) {
            if (brandId.equals(product.getBrandId())) {
                result.add(product);
            }
        }
        return result;
    }

    @Override
    public List<Product> findAllByIds(List<Long> ids) {
        List<Product> result = new ArrayList<>();
        for (Long id : ids) {
            Product product = store.get(id);
            if (product != null) {
                result.add(product);
            }
        }
        return result;
    }
}
