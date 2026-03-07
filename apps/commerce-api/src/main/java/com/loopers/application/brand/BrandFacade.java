package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandService brandService;
    private final ProductService productService;

    public BrandInfo register(String name, String description, String imageUrl) {
        Brand brand = brandService.register(name, description, imageUrl);
        return BrandInfo.from(brand);
    }

    public BrandInfo getBrand(Long id) {
        Brand brand = brandService.getBrand(id);
        return BrandInfo.from(brand);
    }

    public List<BrandInfo> getAllBrands() {
        return brandService.getAllBrands().stream()
            .map(BrandInfo::from)
            .toList();
    }

    public BrandInfo update(Long id, String name, String description, String imageUrl) {
        Brand brand = brandService.update(id, name, description, imageUrl);
        return BrandInfo.from(brand);
    }

    public void delete(Long id) {
        brandService.delete(id);
        productService.deleteAllByBrandId(id);
    }
}
