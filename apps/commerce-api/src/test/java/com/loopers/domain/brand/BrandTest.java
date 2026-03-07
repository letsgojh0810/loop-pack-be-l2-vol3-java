package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrandTest {

    private static final String VALID_NAME = "나이키";
    private static final String VALID_DESCRIPTION = "스포츠 브랜드";
    private static final String VALID_IMAGE_URL = "https://example.com/nike.png";

    @DisplayName("Brand 생성 시,")
    @Nested
    class Create {

        @DisplayName("유효한 이름으로 생성하면, 정상적으로 생성된다.")
        @Test
        void createsBrand_whenNameIsValid() {
            // arrange & act
            Brand brand = new Brand(VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL);

            // assert
            assertAll(
                () -> assertThat(brand.getName()).isEqualTo(VALID_NAME),
                () -> assertThat(brand.getDescription()).isEqualTo(VALID_DESCRIPTION),
                () -> assertThat(brand.getImageUrl()).isEqualTo(VALID_IMAGE_URL)
            );
        }

        @DisplayName("설명과 이미지 URL이 null이어도, 정상적으로 생성된다.")
        @Test
        void createsBrand_whenOptionalFieldsAreNull() {
            // arrange & act & assert
            assertDoesNotThrow(() -> new Brand(VALID_NAME, null, null));
        }

        @DisplayName("이름이 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameIsNull() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                new Brand(null, VALID_DESCRIPTION, VALID_IMAGE_URL)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이름이 빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameIsBlank() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () ->
                new Brand("  ", VALID_DESCRIPTION, VALID_IMAGE_URL)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("Brand 수정 시,")
    @Nested
    class Update {

        @DisplayName("유효한 이름으로 수정하면, 정상적으로 수정된다.")
        @Test
        void updatesBrand_whenNameIsValid() {
            // arrange
            Brand brand = new Brand(VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL);

            // act
            brand.update("아디다스", "또 다른 스포츠 브랜드", "https://example.com/adidas.png");

            // assert
            assertAll(
                () -> assertThat(brand.getName()).isEqualTo("아디다스"),
                () -> assertThat(brand.getDescription()).isEqualTo("또 다른 스포츠 브랜드"),
                () -> assertThat(brand.getImageUrl()).isEqualTo("https://example.com/adidas.png")
            );
        }

        @DisplayName("이름이 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenUpdateNameIsNull() {
            // arrange
            Brand brand = new Brand(VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brand.update(null, VALID_DESCRIPTION, VALID_IMAGE_URL)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이름이 빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenUpdateNameIsBlank() {
            // arrange
            Brand brand = new Brand(VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brand.update("", VALID_DESCRIPTION, VALID_IMAGE_URL)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
