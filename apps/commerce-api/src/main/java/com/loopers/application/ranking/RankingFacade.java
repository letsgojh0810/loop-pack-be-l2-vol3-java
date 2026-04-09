package com.loopers.application.ranking;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class RankingFacade {

    private final RankingService rankingService;
    private final ProductService productService;
    private final BrandService brandService;

    public List<RankingInfo> getTopRankings(String date, int page, int size) {
        List<Long> productIds = rankingService.getTopProductIds(date, page, size);
        if (productIds.isEmpty()) return List.of();

        List<Product> products = productService.getProductsByIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Long> brandIds = products.stream().map(Product::getBrandId).distinct().toList();
        Map<Long, Brand> brandMap = brandService.getBrandsByIds(brandIds);

        List<RankingInfo> result = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            Product product = productMap.get(productId);
            if (product == null) continue;
            Brand brand = brandMap.get(product.getBrandId());
            result.add(new RankingInfo(
                    (long) (page * size + i + 1),
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    product.getPrice(),
                    brand != null ? brand.getId() : null,
                    brand != null ? brand.getName() : null
            ));
        }
        return result;
    }
}
