package com.loopers.domain.order;

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

class OrderServiceTest {

    private FakeOrderRepository orderRepository;
    private FakeOrderItemRepository orderItemRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = new FakeOrderRepository();
        orderItemRepository = new FakeOrderItemRepository();
        orderService = new OrderService(orderRepository, orderItemRepository);
    }

    @DisplayName("주문 생성 시,")
    @Nested
    class CreateOrder {

        @DisplayName("유효한 userId와 항목이 주어지면, 주문이 생성되고 항목에 orderId가 할당된다.")
        @Test
        void createsOrder_andAssignsOrderIdToItems() {
            // arrange
            Long userId = 1L;
            List<OrderItem> items = List.of(
                OrderItem.createSnapshot(1L, "브랜드A", "상품A", 5000, 2),
                OrderItem.createSnapshot(2L, "브랜드B", "상품B", 3000, 1)
            );

            // act
            Order order = orderService.createOrder(userId, items);

            // assert
            assertAll(
                () -> assertThat(order.getId()).isNotNull(),
                () -> assertThat(order.getUserId()).isEqualTo(userId),
                () -> assertThat(order.getTotalAmount()).isEqualTo(5000 * 2 + 3000 * 1),
                () -> items.forEach(item -> assertThat(item.getOrderId()).isEqualTo(order.getId()))
            );
        }
    }

    @DisplayName("주문 조회 시,")
    @Nested
    class GetOrder {

        @DisplayName("존재하는 orderId를 주면, 해당 주문을 반환한다.")
        @Test
        void returnsOrder_whenOrderExists() {
            // arrange
            List<OrderItem> items = List.of(OrderItem.createSnapshot(1L, "브랜드", "상품", 1000, 1));
            Order created = orderService.createOrder(1L, items);

            // act
            Order result = orderService.getOrder(created.getId());

            // assert
            assertThat(result.getId()).isEqualTo(created.getId());
        }

        @DisplayName("존재하지 않는 orderId를 주면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenOrderDoesNotExist() {
            // act
            CoreException result = assertThrows(CoreException.class, () ->
                orderService.getOrder(999L)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("주문 항목 조회 시,")
    @Nested
    class GetOrderItems {

        @DisplayName("orderId에 해당하는 항목 목록을 반환한다.")
        @Test
        void returnsOrderItems_forGivenOrderId() {
            // arrange
            List<OrderItem> items = List.of(
                OrderItem.createSnapshot(1L, "브랜드A", "상품A", 1000, 2),
                OrderItem.createSnapshot(2L, "브랜드B", "상품B", 2000, 1)
            );
            Order order = orderService.createOrder(1L, items);

            // act
            List<OrderItem> result = orderService.getOrderItems(order.getId());

            // assert
            assertThat(result).hasSize(2);
        }
    }

    @DisplayName("사용자별 주문 조회 시,")
    @Nested
    class GetOrdersByUserId {

        @DisplayName("userId에 해당하는 주문 목록을 반환한다.")
        @Test
        void returnsOrders_forGivenUserId() {
            // arrange
            Long userId = 1L;
            List<OrderItem> items1 = List.of(OrderItem.createSnapshot(1L, "브랜드", "상품A", 1000, 1));
            List<OrderItem> items2 = List.of(OrderItem.createSnapshot(2L, "브랜드", "상품B", 2000, 1));
            orderService.createOrder(userId, items1);
            orderService.createOrder(userId, items2);
            orderService.createOrder(2L, List.of(OrderItem.createSnapshot(3L, "브랜드", "상품C", 500, 1)));

            // act
            List<Order> result = orderService.getOrdersByUserId(userId);

            // assert
            assertAll(
                () -> assertThat(result).hasSize(2),
                () -> result.forEach(order -> assertThat(order.getUserId()).isEqualTo(userId))
            );
        }
    }
}
