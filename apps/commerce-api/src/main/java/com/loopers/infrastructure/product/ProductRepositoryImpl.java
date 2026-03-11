package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public List<Product> findAllPaged(Long brandId, ProductSort sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, toSort(sort));
        if (brandId != null) {
            return productJpaRepository.findAllByBrandIdAndDeletedAtIsNull(brandId, pageable).getContent();
        }
        return productJpaRepository.findAllByDeletedAtIsNull(pageable).getContent();
    }

    private Sort toSort(ProductSort sort) {
        return switch (sort) {
            case LATEST -> Sort.by("createdAt").descending();
            case PRICE_ASC -> Sort.by("price").ascending();
            case LIKES_DESC -> Sort.by("likeCount").descending();
        };
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
