package com.loopers.domain.brand;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeBrandRepository implements BrandRepository {

    private final Map<Long, Brand> store = new HashMap<>();
    private Long sequence = 1L;

    @Override
    public Brand save(Brand brand) {
        if (brand.getId() == null || brand.getId() == 0L) {
            ReflectionTestUtils.setField(brand, "id", sequence++);
        }
        store.put(brand.getId(), brand);
        return brand;
    }

    @Override
    public Optional<Brand> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Brand> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Brand> findByName(String name) {
        return store.values().stream()
            .filter(brand -> brand.getName().equals(name))
            .findFirst();
    }

    @Override
    public boolean existsByName(String name) {
        return store.values().stream()
            .anyMatch(brand -> brand.getName().equals(name));
    }

    @Override
    public List<Brand> findAllByIds(List<Long> ids) {
        return store.values().stream()
            .filter(brand -> ids.contains(brand.getId()))
            .toList();
    }
}
