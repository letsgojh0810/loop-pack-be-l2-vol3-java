package com.loopers.application.brand;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.brand.FakeBrandRepository;
import com.loopers.domain.product.FakeProductRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class BrandFacadeTest {

    private BrandFacade brandFacade;
    private FakeBrandRepository fakeBrandRepository;
    private FakeProductRepository fakeProductRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        fakeBrandRepository = new FakeBrandRepository();
        fakeProductRepository = new FakeProductRepository();

        BrandService brandService = new BrandService(fakeBrandRepository);
        productService = new ProductService(fakeProductRepository);

        brandFacade = new BrandFacade(brandService, productService);
    }

    @DisplayName("브랜드 등록 시,")
    @Nested
    class Register {

        @DisplayName("브랜드가 정상적으로 등록된다.")
        @Test
        void registersBrand_successfully() {
            // arrange
            String name = "나이키";
            String description = "스포츠 브랜드";
            String imageUrl = "http://nike.url";

            // act
            BrandInfo result = brandFacade.register(name, description, imageUrl);

            // assert
            assertAll(
                () -> assertThat(result.brandId()).isNotNull(),
                () -> assertThat(result.name()).isEqualTo("나이키"),
                () -> assertThat(result.description()).isEqualTo("스포츠 브랜드"),
                () -> assertThat(result.imageUrl()).isEqualTo("http://nike.url")
            );
        }
    }

    @DisplayName("브랜드 조회 시,")
    @Nested
    class GetBrand {

        @DisplayName("존재하는 브랜드 ID로 브랜드 정보를 반환한다.")
        @Test
        void returnsBrandInfo_whenBrandExists() {
            // arrange
            BrandInfo registered = brandFacade.register("아디다스", "스포츠 브랜드", "http://adidas.url");

            // act
            BrandInfo result = brandFacade.getBrand(registered.brandId());

            // assert
            assertAll(
                () -> assertThat(result.brandId()).isEqualTo(registered.brandId()),
                () -> assertThat(result.name()).isEqualTo("아디다스")
            );
        }
    }

    @DisplayName("브랜드 삭제 시,")
    @Nested
    class Delete {

        @DisplayName("브랜드 삭제 시 해당 브랜드의 모든 상품이 소프트 삭제된다.")
        @Test
        void deleteBrand_cascadesToProducts() {
            // arrange
            BrandInfo brand = brandFacade.register("뉴발란스", "라이프스타일 브랜드", "http://nb.url");
            Long brandId = brand.brandId();

            Product product1 = fakeProductRepository.save(new Product(brandId, "550", "클래식 운동화", 120000, 10, "http://550.url"));
            Product product2 = fakeProductRepository.save(new Product(brandId, "574", "데일리 운동화", 110000, 5, "http://574.url"));

            // act
            brandFacade.delete(brandId);

            // assert
            List<Product> products = fakeProductRepository.findAllByBrandId(brandId);
            assertAll(
                () -> assertThat(products).hasSize(2),
                () -> assertThat(products.get(0).getDeletedAt()).isNotNull(),
                () -> assertThat(products.get(1).getDeletedAt()).isNotNull()
            );
        }
    }
}
