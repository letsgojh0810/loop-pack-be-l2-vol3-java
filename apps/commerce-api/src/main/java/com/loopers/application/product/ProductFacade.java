package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.infrastructure.cache.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;
    private final ProductLikeService productLikeService;

    @Cacheable(cacheNames = CacheConfig.PRODUCT_DETAIL, key = "#productId")
    public ProductInfo getProductDetail(Long productId, Long userId) {
        Product product = productService.getProduct(productId);
        Brand brand = brandService.getBrand(product.getBrandId());
        boolean liked = userId != null && productLikeService.isLiked(userId, productId);
        return ProductInfo.of(product, brand, product.getLikeCount(), liked);
    }

    @Cacheable(cacheNames = CacheConfig.PRODUCT_LIST, key = "#brandId != null ? #brandId : 'all'")
    public List<ProductInfo> getProducts(Long brandId) {
        List<Product> products = brandId != null
            ? productService.getProductsByBrandId(brandId)
            : productService.getAllProducts();
        return products.stream()
            .map(product -> {
                Brand brand = brandService.getBrand(product.getBrandId());
                return ProductInfo.of(product, brand, product.getLikeCount(), false);
            })
            .toList();
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_LIST, allEntries = true)
    })
    public ProductInfo registerProduct(Long brandId, String name, String description, int price, int stock, String imageUrl) {
        brandService.getBrand(brandId);
        Product product = productService.register(brandId, name, description, price, stock, imageUrl);
        Brand brand = brandService.getBrand(product.getBrandId());
        return ProductInfo.of(product, brand, 0L, false);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_DETAIL, key = "#productId"),
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_LIST, allEntries = true)
    })
    public ProductInfo updateProduct(Long productId, String name, String description, int price, int stock, String imageUrl) {
        Product product = productService.update(productId, name, description, price, stock, imageUrl);
        Brand brand = brandService.getBrand(product.getBrandId());
        return ProductInfo.of(product, brand, product.getLikeCount(), false);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_DETAIL, key = "#productId"),
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_LIST, allEntries = true)
    })
    public void deleteProduct(Long productId) {
        productService.delete(productId);
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_DETAIL, key = "#productId"),
        @CacheEvict(cacheNames = CacheConfig.PRODUCT_LIST, allEntries = true)
    })
    public void evictProductCache(Long productId) {
        // 좋아요 변경 시 캐시 무효화용
    }
}
