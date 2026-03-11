package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    Optional<Product> findByIdForUpdate(Long id);
    List<Product> findAll();
    List<Product> findAllPaged(Long brandId, ProductSort sort, int page, int size);
    List<Product> findAllOrderByLikeCountDesc();
    List<Product> findAllByBrandId(Long brandId);
    List<Product> findAllByBrandIdOrderByLikeCountDesc(Long brandId);
    List<Product> findAllByIds(List<Long> ids);
}
