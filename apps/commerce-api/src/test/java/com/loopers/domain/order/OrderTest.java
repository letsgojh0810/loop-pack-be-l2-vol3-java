package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    private static OrderItem validItem(int price, int quantity) {
        return OrderItem.createSnapshot(1L, "브랜드", "상품", price, quantity);
    }

    @DisplayName("Order 생성 시,")
    @Nested
    class Create {

        @DisplayName("유효한 userId와 항목 목록이 주어지면, 정상적으로 생성된다.")
        @Test
        void createsOrder_whenValidUserIdAndItemsAreProvided() {
            // arrange
            List<OrderItem> items = List.of(validItem(1000, 2), validItem(500, 3));

            // act
            Order order = Order.create(1L, items);

            // assert
            assertAll(
                () -> assertThat(order.getUserId()).isEqualTo(1L),
                () -> assertThat(order.getTotalAmount()).isEqualTo(3500)
            );
        }

        @DisplayName("userId가 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenUserIdIsNull() {
            // arrange
            List<OrderItem> items = List.of(validItem(1000, 1));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                Order.create(null, items)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("항목 목록이 비어있으면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenItemsAreEmpty() {
            // arrange
            List<OrderItem> items = List.of();

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                Order.create(1L, items)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("totalAmount가 항목들의 가격 * 수량 합계로 계산된다.")
        @Test
        void calculatesTotalAmount_fromItemsPriceAndQuantity() {
            // arrange
            List<OrderItem> items = List.of(
                validItem(2000, 3),
                validItem(1000, 5)
            );

            // act
            Order order = Order.create(1L, items);

            // assert
            assertThat(order.getTotalAmount()).isEqualTo(2000 * 3 + 1000 * 5);
        }
    }
}
