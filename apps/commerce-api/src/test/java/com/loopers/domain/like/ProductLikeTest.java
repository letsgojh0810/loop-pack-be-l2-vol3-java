package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductLikeTest {

    private static final Long VALID_USER_ID = 1L;
    private static final Long VALID_PRODUCT_ID = 100L;

    @DisplayName("ProductLike 생성 시,")
    @Nested
    class Create {

        @DisplayName("유효한 userId와 productId로 생성하면, 정상적으로 생성된다.")
        @Test
        void creates_whenAllFieldsAreValid() {
            // arrange & act
            ProductLike productLike = new ProductLike(VALID_USER_ID, VALID_PRODUCT_ID);

            // assert
            assertAll(
                () -> assertThat(productLike.getUserId()).isEqualTo(VALID_USER_ID),
                () -> assertThat(productLike.getProductId()).isEqualTo(VALID_PRODUCT_ID),
                () -> assertThat(productLike.getCreatedAt()).isNotNull()
            );
        }

        @DisplayName("userId가 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenUserIdIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                new ProductLike(null, VALID_PRODUCT_ID)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("productId가 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenProductIdIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                new ProductLike(VALID_USER_ID, null)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
