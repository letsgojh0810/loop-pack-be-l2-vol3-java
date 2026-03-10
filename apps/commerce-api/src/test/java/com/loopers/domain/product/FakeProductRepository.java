package com.loopers.domain.product;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Comparator;
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
    public List<Product> findAllPaged(Long brandId, ProductSort sort, int page, int size) {
        Comparator<Product> comparator = switch (sort) {
            case LATEST -> Comparator.comparing(Product::getCreatedAt).reversed();
            case PRICE_ASC -> Comparator.comparingInt(Product::getPrice);
            case LIKES_DESC -> Comparator.comparingLong(Product::getLikeCount).reversed();
        };
        return store.values().stream()
            .filter(p -> p.getDeletedAt() == null)
            .filter(p -> brandId == null || brandId.equals(p.getBrandId()))
            .sorted(comparator)
            .skip((long) page * size)
            .limit(size)
            .toList();
    }

    @Override
    public List<Product> findAllOrderByLikeCountDesc() {
        return store.values().stream()
            .sorted((a, b) -> Long.compare(b.getLikeCount(), a.getLikeCount()))
            .toList();
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
    public List<Product> findAllByBrandIdOrderByLikeCountDesc(Long brandId) {
        return store.values().stream()
            .filter(p -> brandId.equals(p.getBrandId()))
            .sorted((a, b) -> Long.compare(b.getLikeCount(), a.getLikeCount()))
            .toList();
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

    @Override
    public Optional<Product> findByIdForUpdate(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}
