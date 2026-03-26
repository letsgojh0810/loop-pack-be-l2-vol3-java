package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductSort;
import com.loopers.infrastructure.cache.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        long likeCount = productLikeService.getLikeCount(productId);
        return ProductInfo.of(product, brand, likeCount, liked);
    }

    @Cacheable(
        cacheNames = CacheConfig.PRODUCT_LIST,
        key = "(#brandId ?: 'all') + '_' + #sort + '_' + #page + '_' + #size"
    )
    public List<ProductInfo> getProducts(Long brandId, ProductSort sort, int page, int size) {
        List<Product> products = productService.getProducts(brandId, sort, page, size);

        List<Long> brandIds = products.stream().map(Product::getBrandId).distinct().toList();
        Map<Long, Brand> brandMap = brandService.getBrandsByIds(brandIds);

        List<ProductInfo> result = new ArrayList<>();
        for (Product product : products) {
            result.add(ProductInfo.of(product, brandMap.get(product.getBrandId()), product.getLikeCount(), false));
        }
        return result;
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

}
