package com.loopers.domain.brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepository {
    Brand save(Brand brand);
    Optional<Brand> findById(Long id);
    List<Brand> findAll();
    Optional<Brand> findByName(String name);
    boolean existsByName(String name);
    List<Brand> findAllByIds(List<Long> ids);
}
