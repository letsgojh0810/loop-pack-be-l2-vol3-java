package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product register(Long brandId, String name, String description, int price, int stock, String imageUrl) {
        Product product = new Product(brandId, name, description, price, stock, imageUrl);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByBrandId(Long brandId) {
        return productRepository.findAllByBrandId(brandId);
    }

    @Transactional
    public Product update(Long id, String name, String description, int price, int stock, String imageUrl) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.update(name, description, price, stock, imageUrl);
        return product;
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.delete();
    }

    @Transactional
    public void deleteAllByBrandId(Long brandId) {
        List<Product> products = productRepository.findAllByBrandId(brandId);
        for (Product product : products) {
            product.delete();
        }
    }

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.decreaseStock(quantity);
    }
}
