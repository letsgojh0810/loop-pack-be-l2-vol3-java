package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    private static final Long VALID_BRAND_ID = 1L;
    private static final String VALID_NAME = "테스트 상품";
    private static final String VALID_DESCRIPTION = "상품 설명";
    private static final int VALID_PRICE = 10000;
    private static final int VALID_STOCK = 100;
    private static final String VALID_IMAGE_URL = "https://example.com/image.jpg";

    @DisplayName("Product 생성 시,")
    @Nested
    class Create {

        @DisplayName("모든 필드가 유효하면, 정상적으로 생성된다.")
        @Test
        void createsProduct_whenAllFieldsAreValid() {
            // arrange & act
            Product product = new Product(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, VALID_STOCK, VALID_IMAGE_URL);

            // assert
            assertAll(
                () -> assertThat(product.getBrandId()).isEqualTo(VALID_BRAND_ID),
                () -> assertThat(product.getName()).isEqualTo(VALID_NAME),
                () -> assertThat(product.getDescription()).isEqualTo(VALID_DESCRIPTION),
                () -> assertThat(product.getPrice()).isEqualTo(VALID_PRICE),
                () -> assertThat(product.getStock()).isEqualTo(VALID_STOCK),
                () -> assertThat(product.getImageUrl()).isEqualTo(VALID_IMAGE_URL)
            );
        }

        @DisplayName("brandId가 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenBrandIdIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                new Product(null, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, VALID_STOCK, VALID_IMAGE_URL)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("name이 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                new Product(VALID_BRAND_ID, null, VALID_DESCRIPTION, VALID_PRICE, VALID_STOCK, VALID_IMAGE_URL)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("name이 공백이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameIsBlank() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                new Product(VALID_BRAND_ID, "   ", VALID_DESCRIPTION, VALID_PRICE, VALID_STOCK, VALID_IMAGE_URL)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("price가 음수이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenPriceIsNegative() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                new Product(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, -1, VALID_STOCK, VALID_IMAGE_URL)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("stock이 음수이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenStockIsNegative() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                new Product(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, -1, VALID_IMAGE_URL)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("재고 감소 시,")
    @Nested
    class DecreaseStock {

        @DisplayName("재고가 충분하면, 재고가 감소한다.")
        @Test
        void decreasesStock_whenStockIsSufficient() {
            // arrange
            Product product = new Product(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, 10, VALID_IMAGE_URL);

            // act
            assertDoesNotThrow(() -> product.decreaseStock(5));

            // assert
            assertThat(product.getStock()).isEqualTo(5);
        }

        @DisplayName("재고가 부족하면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenStockIsInsufficient() {
            // arrange
            Product product = new Product(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, 3, VALID_IMAGE_URL);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                product.decreaseStock(5)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("재고 확인 시,")
    @Nested
    class HasEnoughStock {

        @DisplayName("재고가 충분하면, true를 반환한다.")
        @Test
        void returnsTrue_whenStockIsSufficient() {
            // arrange
            Product product = new Product(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, 10, VALID_IMAGE_URL);

            // act
            boolean result = product.hasEnoughStock(10);

            // assert
            assertThat(result).isTrue();
        }

        @DisplayName("재고가 부족하면, false를 반환한다.")
        @Test
        void returnsFalse_whenStockIsInsufficient() {
            // arrange
            Product product = new Product(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, 3, VALID_IMAGE_URL);

            // act
            boolean result = product.hasEnoughStock(5);

            // assert
            assertThat(result).isFalse();
        }
    }

    @DisplayName("상품 수정 시,")
    @Nested
    class Update {

        @DisplayName("유효한 값으로 수정하면, 상품 정보가 변경된다.")
        @Test
        void updatesProduct_whenValidValuesAreProvided() {
            // arrange
            Product product = new Product(VALID_BRAND_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, VALID_STOCK, VALID_IMAGE_URL);
            String newName = "수정된 상품명";
            String newDescription = "수정된 설명";
            int newPrice = 20000;
            int newStock = 50;
            String newImageUrl = "https://example.com/new-image.jpg";

            // act
            product.update(newName, newDescription, newPrice, newStock, newImageUrl);

            // assert
            assertAll(
                () -> assertThat(product.getName()).isEqualTo(newName),
                () -> assertThat(product.getDescription()).isEqualTo(newDescription),
                () -> assertThat(product.getPrice()).isEqualTo(newPrice),
                () -> assertThat(product.getStock()).isEqualTo(newStock),
                () -> assertThat(product.getImageUrl()).isEqualTo(newImageUrl)
            );
        }
    }
}
