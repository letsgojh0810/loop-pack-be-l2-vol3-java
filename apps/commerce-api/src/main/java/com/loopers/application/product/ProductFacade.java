package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;
    private final ProductLikeService productLikeService;

    public ProductInfo getProductDetail(Long productId, Long userId) {
        Product product = productService.getProduct(productId);
        Brand brand = brandService.getBrand(product.getBrandId());
        long likeCount = productLikeService.getLikeCount(productId);
        boolean liked = userId != null && productLikeService.isLiked(userId, productId);
        return ProductInfo.of(product, brand, likeCount, liked);
    }

    public List<ProductInfo> getProducts(Long brandId) {
        List<Product> products;
        if (brandId != null) {
            products = productService.getProductsByBrandId(brandId);
        } else {
            products = productService.getAllProducts();
        }
        return products.stream()
            .map(product -> {
                Brand brand = brandService.getBrand(product.getBrandId());
                long likeCount = productLikeService.getLikeCount(product.getId());
                return ProductInfo.of(product, brand, likeCount, false);
            })
            .toList();
    }

    public ProductInfo registerProduct(Long brandId, String name, String description, int price, int stock, String imageUrl) {
        brandService.getBrand(brandId);
        Product product = productService.register(brandId, name, description, price, stock, imageUrl);
        Brand brand = brandService.getBrand(product.getBrandId());
        return ProductInfo.of(product, brand, 0L, false);
    }

    public ProductInfo updateProduct(Long productId, String name, String description, int price, int stock, String imageUrl) {
        Product product = productService.update(productId, name, description, price, stock, imageUrl);
        Brand brand = brandService.getBrand(product.getBrandId());
        long likeCount = productLikeService.getLikeCount(productId);
        return ProductInfo.of(product, brand, likeCount, false);
    }

    public void deleteProduct(Long productId) {
        productService.delete(productId);
    }
}
