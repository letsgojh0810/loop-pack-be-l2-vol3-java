package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @DisplayName("OrderItem 스냅샷 생성 시,")
    @Nested
    class CreateSnapshot {

        @DisplayName("유효한 값이 주어지면, 정상적으로 생성된다.")
        @Test
        void createsSnapshot_whenAllFieldsAreValid() {
            // arrange & act
            OrderItem item = OrderItem.createSnapshot(1L, "나이키", "에어맥스", 100000, 2);

            // assert
            assertAll(
                () -> assertThat(item.getProductId()).isEqualTo(1L),
                () -> assertThat(item.getBrandName()).isEqualTo("나이키"),
                () -> assertThat(item.getProductName()).isEqualTo("에어맥스"),
                () -> assertThat(item.getPrice()).isEqualTo(100000),
                () -> assertThat(item.getQuantity()).isEqualTo(2)
            );
        }

        @DisplayName("productId가 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenProductIdIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class, () ->
                OrderItem.createSnapshot(null, "브랜드", "상품", 1000, 1)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("brandName이 공백이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenBrandNameIsBlank() {
            // act
            CoreException result = assertThrows(CoreException.class, () ->
                OrderItem.createSnapshot(1L, "  ", "상품", 1000, 1)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("productName이 공백이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenProductNameIsBlank() {
            // act
            CoreException result = assertThrows(CoreException.class, () ->
                OrderItem.createSnapshot(1L, "브랜드", "  ", 1000, 1)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("price가 음수이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenPriceIsNegative() {
            // act
            CoreException result = assertThrows(CoreException.class, () ->
                OrderItem.createSnapshot(1L, "브랜드", "상품", -1, 1)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("quantity가 0이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenQuantityIsZero() {
            // act
            CoreException result = assertThrows(CoreException.class, () ->
                OrderItem.createSnapshot(1L, "브랜드", "상품", 1000, 0)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("quantity가 음수이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenQuantityIsNegative() {
            // act
            CoreException result = assertThrows(CoreException.class, () ->
                OrderItem.createSnapshot(1L, "브랜드", "상품", 1000, -1)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("orderId 할당 시,")
    @Nested
    class AssignOrderId {

        @DisplayName("orderId가 정상적으로 설정된다.")
        @Test
        void assignsOrderId_correctly() {
            // arrange
            OrderItem item = OrderItem.createSnapshot(1L, "브랜드", "상품", 1000, 1);

            // act
            item.assignOrderId(42L);

            // assert
            assertThat(item.getOrderId()).isEqualTo(42L);
        }
    }
}
