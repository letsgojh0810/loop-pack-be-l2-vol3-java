package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public Optional<Product> findByIdForUpdate(Long id) {
        return productJpaRepository.findByIdForUpdate(id);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll();
    }

    @Override
    public List<Product> findAllOrderByLikeCountDesc() {
        return productJpaRepository.findAllByOrderByLikeCountDesc();
    }

    @Override
    public List<Product> findAllByBrandId(Long brandId) {
        return productJpaRepository.findAllByBrandId(brandId);
    }

    @Override
    public List<Product> findAllByBrandIdOrderByLikeCountDesc(Long brandId) {
        return productJpaRepository.findAllByBrandIdOrderByLikeCountDesc(brandId);
    }

    @Override
    public List<Product> findAllByIds(List<Long> ids) {
        return productJpaRepository.findAllById(ids);
    }
}
