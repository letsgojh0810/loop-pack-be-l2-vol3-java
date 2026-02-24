package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.brand.FakeBrandRepository;
import com.loopers.domain.like.FakeProductLikeRepository;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.FakeProductRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductFacadeTest {

    private ProductFacade productFacade;
    private FakeBrandRepository fakeBrandRepository;
    private FakeProductRepository fakeProductRepository;
    private FakeProductLikeRepository fakeProductLikeRepository;
    private ProductLikeService productLikeService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        fakeBrandRepository = new FakeBrandRepository();
        fakeProductRepository = new FakeProductRepository();
        fakeProductLikeRepository = new FakeProductLikeRepository();

        BrandService brandService = new BrandService(fakeBrandRepository);
        ProductService productService = new ProductService(fakeProductRepository);
        productLikeService = new ProductLikeService(fakeProductLikeRepository);

        productFacade = new ProductFacade(productService, brandService, productLikeService);
    }

    @DisplayName("상품 상세 조회 시,")
    @Nested
    class GetProductDetail {

        @DisplayName("상품, 브랜드, 좋아요 정보가 통합되어 반환된다.")
        @Test
        void returnsProductDetail_withCombinedInfo() {
            // arrange
            Brand brand = fakeBrandRepository.save(new Brand("나이키", "스포츠 브랜드", "http://nike.url"));
            Product product = fakeProductRepository.save(new Product(brand.getId(), "에어맥스", "운동화", 150000, 20, "http://airmax.url"));

            productLikeService.like(USER_ID, product.getId());
            productLikeService.like(2L, product.getId());

            // act
            ProductInfo result = productFacade.getProductDetail(product.getId(), USER_ID);

            // assert
            assertAll(
                () -> assertThat(result.productId()).isEqualTo(product.getId()),
                () -> assertThat(result.productName()).isEqualTo("에어맥스"),
                () -> assertThat(result.description()).isEqualTo("운동화"),
                () -> assertThat(result.price()).isEqualTo(150000),
                () -> assertThat(result.stock()).isEqualTo(20),
                () -> assertThat(result.brandId()).isEqualTo(brand.getId()),
                () -> assertThat(result.brandName()).isEqualTo("나이키"),
                () -> assertThat(result.likeCount()).isEqualTo(2L),
                () -> assertThat(result.liked()).isTrue()
            );
        }

        @DisplayName("존재하지 않는 상품 조회 시 NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenProductDoesNotExist() {
            // arrange
            Long nonExistentProductId = 999L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                productFacade.getProductDetail(nonExistentProductId, USER_ID)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @DisplayName("좋아요하지 않은 사용자에 대해 liked가 false로 반환된다.")
        @Test
        void returnsLikedFalse_whenUserHasNotLiked() {
            // arrange
            Brand brand = fakeBrandRepository.save(new Brand("아디다스", "스포츠 브랜드", "http://adidas.url"));
            Product product = fakeProductRepository.save(new Product(brand.getId(), "슈퍼스타", "클래식 운동화", 120000, 30, "http://superstar.url"));

            productLikeService.like(2L, product.getId());

            // act
            ProductInfo result = productFacade.getProductDetail(product.getId(), USER_ID);

            // assert
            assertAll(
                () -> assertThat(result.likeCount()).isEqualTo(1L),
                () -> assertThat(result.liked()).isFalse()
            );
        }
    }
}
