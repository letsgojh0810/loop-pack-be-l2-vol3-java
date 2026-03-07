package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductServiceTest {

    private ProductService productService;
    private FakeProductRepository fakeProductRepository;

    private static final Long VALID_BRAND_ID = 1L;
    private static final String VALID_NAME = "테스트 상품";
    private static final String VALID_DESCRIPTION = "상품 설명";
    private static final int VALID_PRICE = 10000;
    private static final int VALID_STOCK = 100;
    private static final String VALID_IMAGE_URL = "https://example.com/image.jpg";

    @BeforeEach
    void setUp() {
        fakeProductRepository = new FakeProductRepository();
        productService = new ProductService(fakeProductRepository);
    }

    @DisplayName("상품 등록 시,")
    @Nested
    class Register {

        @DisplayName("유효한 정보로 등록하면, 상품이 생성된다.")
        @Test
        void createsProduct_whenValidInfoIsProvided() {
            // arrange & act
            Product result = productService.register(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, VALID_STOCK, VALID_IMAGE_URL);

            // assert
            assertAll(
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getBrandId()).isEqualTo(VALID_BRAND_ID),
                () -> assertThat(result.getName()).isEqualTo(VALID_NAME)
            );
        }
    }

    @DisplayName("상품 조회 시,")
    @Nested
    class GetProduct {

        @DisplayName("존재하는 ID로 조회하면, 상품 정보를 반환한다.")
        @Test
        void returnsProduct_whenProductExists() {
            // arrange
            Product saved = productService.register(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, VALID_STOCK, VALID_IMAGE_URL);

            // act
            Product result = productService.getProduct(saved.getId());

            // assert
            assertAll(
                () -> assertThat(result.getId()).isEqualTo(saved.getId()),
                () -> assertThat(result.getName()).isEqualTo(VALID_NAME)
            );
        }

        @DisplayName("존재하지 않는 ID로 조회하면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenProductDoesNotExist() {
            // arrange
            Long nonExistentId = 999L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                productService.getProduct(nonExistentId)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("상품 수정 시,")
    @Nested
    class Update {

        @DisplayName("존재하는 ID로 수정하면, 상품 정보가 변경된다.")
        @Test
        void updatesProduct_whenProductExists() {
            // arrange
            Product saved = productService.register(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, VALID_STOCK, VALID_IMAGE_URL);
            String newName = "수정된 상품명";

            // act
            Product result = productService.update(saved.getId(), newName, "새 설명", 20000, 50, "https://example.com/new.jpg");

            // assert
            assertThat(result.getName()).isEqualTo(newName);
        }

        @DisplayName("존재하지 않는 ID로 수정하면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenProductDoesNotExist() {
            // arrange
            Long nonExistentId = 999L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                productService.update(nonExistentId, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, VALID_STOCK, VALID_IMAGE_URL)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("상품 삭제 시,")
    @Nested
    class Delete {

        @DisplayName("존재하는 ID로 삭제하면, 소프트 삭제된다.")
        @Test
        void deletesProduct_whenProductExists() {
            // arrange
            Product saved = productService.register(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, VALID_STOCK, VALID_IMAGE_URL);

            // act
            productService.delete(saved.getId());

            // assert
            Product result = fakeProductRepository.findById(saved.getId()).orElseThrow();
            assertThat(result.getDeletedAt()).isNotNull();
        }

        @DisplayName("존재하지 않는 ID로 삭제하면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenProductDoesNotExist() {
            // arrange
            Long nonExistentId = 999L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                productService.delete(nonExistentId)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("재고 감소 시,")
    @Nested
    class DecreaseStock {

        @DisplayName("재고가 충분하면, 재고가 감소한다.")
        @Test
        void decreasesStock_whenStockIsSufficient() {
            // arrange
            Product saved = productService.register(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, 10, VALID_IMAGE_URL);

            // act
            productService.decreaseStock(saved.getId(), 5);

            // assert
            Product result = productService.getProduct(saved.getId());
            assertThat(result.getStock()).isEqualTo(5);
        }

        @DisplayName("재고가 부족하면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenStockIsInsufficient() {
            // arrange
            Product saved = productService.register(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, 3, VALID_IMAGE_URL);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                productService.decreaseStock(saved.getId(), 5)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("존재하지 않는 ID로 재고 감소하면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenProductDoesNotExist() {
            // arrange
            Long nonExistentId = 999L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                productService.decreaseStock(nonExistentId, 1)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("브랜드별 상품 일괄 삭제 시,")
    @Nested
    class DeleteAllByBrandId {

        @DisplayName("해당 브랜드의 모든 상품이 소프트 삭제된다.")
        @Test
        void deletesAllProducts_whenBrandIdIsGiven() {
            // arrange
            productService.register(VALID_BRAND_ID, "상품1", null, 1000, 10, null);
            productService.register(VALID_BRAND_ID, "상품2", null, 2000, 20, null);
            productService.register(2L, "다른브랜드상품", null, 3000, 30, null);

            // act
            productService.deleteAllByBrandId(VALID_BRAND_ID);

            // assert
            List<Product> brandProducts = fakeProductRepository.findAllByBrandId(VALID_BRAND_ID);
            assertThat(brandProducts).allMatch(p -> p.getDeletedAt() != null);
        }
    }

    @DisplayName("브랜드별 상품 조회 시,")
    @Nested
    class GetProductsByBrandId {

        @DisplayName("해당 브랜드의 상품 목록을 반환한다.")
        @Test
        void returnsProducts_whenBrandIdIsGiven() {
            // arrange
            productService.register(VALID_BRAND_ID, "상품1", null, 1000, 10, null);
            productService.register(VALID_BRAND_ID, "상품2", null, 2000, 20, null);
            productService.register(2L, "다른브랜드상품", null, 3000, 30, null);

            // act
            List<Product> result = productService.getProductsByBrandId(VALID_BRAND_ID);

            // assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(p -> VALID_BRAND_ID.equals(p.getBrandId()));
        }
    }
}
