package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.brand.FakeBrandRepository;
import com.loopers.domain.order.FakeOrderItemRepository;
import com.loopers.domain.order.FakeOrderRepository;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.FakeProductRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
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

class OrderFacadeTest {

    private OrderFacade orderFacade;
    private FakeBrandRepository fakeBrandRepository;
    private FakeProductRepository fakeProductRepository;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        fakeBrandRepository = new FakeBrandRepository();
        fakeProductRepository = new FakeProductRepository();
        FakeOrderRepository fakeOrderRepository = new FakeOrderRepository();
        FakeOrderItemRepository fakeOrderItemRepository = new FakeOrderItemRepository();

        BrandService brandService = new BrandService(fakeBrandRepository);
        ProductService productService = new ProductService(fakeProductRepository);
        OrderService orderService = new OrderService(fakeOrderRepository, fakeOrderItemRepository);

        orderFacade = new OrderFacade(productService, brandService, orderService);
    }

    @DisplayName("주문 생성 시,")
    @Nested
    class CreateOrder {

        @DisplayName("상품과 재고가 충분하면 주문이 정상적으로 생성된다.")
        @Test
        void createsOrder_whenProductExistsAndStockSufficient() {
            // arrange
            Brand brand = fakeBrandRepository.save(new Brand("브랜드명", "브랜드 설명", "http://brand.url"));
            Product product = fakeProductRepository.save(new Product(brand.getId(), "상품명", "상품 설명", 5000, 10, "http://product.url"));

            List<OrderCreateItem> items = List.of(new OrderCreateItem(product.getId(), 2));

            // act
            OrderInfo result = orderFacade.createOrder(USER_ID, items);

            // assert
            assertAll(
                () -> assertThat(result.orderId()).isNotNull(),
                () -> assertThat(result.userId()).isEqualTo(USER_ID),
                () -> assertThat(result.totalAmount()).isEqualTo(10000),
                () -> assertThat(result.items()).hasSize(1),
                () -> assertThat(result.items().get(0).productId()).isEqualTo(product.getId()),
                () -> assertThat(result.items().get(0).brandName()).isEqualTo("브랜드명"),
                () -> assertThat(result.items().get(0).productName()).isEqualTo("상품명"),
                () -> assertThat(result.items().get(0).quantity()).isEqualTo(2),
                () -> assertThat(product.getStock()).isEqualTo(8)
            );
        }

        @DisplayName("재고가 부족하면 BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenStockInsufficient() {
            // arrange
            Brand brand = fakeBrandRepository.save(new Brand("브랜드명", "브랜드 설명", "http://brand.url"));
            fakeProductRepository.save(new Product(brand.getId(), "상품명", "상품 설명", 5000, 1, "http://product.url"));
            Product product = fakeProductRepository.save(new Product(brand.getId(), "상품명", "상품 설명", 5000, 1, "http://product.url"));

            List<OrderCreateItem> items = List.of(new OrderCreateItem(product.getId(), 5));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                orderFacade.createOrder(USER_ID, items)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("존재하지 않는 상품으로 주문 시 NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenProductDoesNotExist() {
            // arrange
            List<OrderCreateItem> items = List.of(new OrderCreateItem(999L, 1));

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                orderFacade.createOrder(USER_ID, items)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("주문 조회 시,")
    @Nested
    class GetOrder {

        @DisplayName("존재하는 주문 ID로 주문 정보를 정상적으로 반환한다.")
        @Test
        void returnsOrder_whenOrderExists() {
            // arrange
            Brand brand = fakeBrandRepository.save(new Brand("브랜드명", "브랜드 설명", "http://brand.url"));
            Product product = fakeProductRepository.save(new Product(brand.getId(), "상품명", "상품 설명", 3000, 10, "http://product.url"));
            List<OrderCreateItem> createItems = List.of(new OrderCreateItem(product.getId(), 1));
            OrderInfo created = orderFacade.createOrder(USER_ID, createItems);

            // act
            OrderInfo result = orderFacade.getOrder(created.orderId());

            // assert
            assertAll(
                () -> assertThat(result.orderId()).isEqualTo(created.orderId()),
                () -> assertThat(result.userId()).isEqualTo(USER_ID),
                () -> assertThat(result.items()).hasSize(1)
            );
        }
    }

    @DisplayName("사용자별 주문 목록 조회 시,")
    @Nested
    class GetOrdersByUserId {

        @DisplayName("해당 사용자의 모든 주문을 반환한다.")
        @Test
        void returnsAllOrdersForUser() {
            // arrange
            Brand brand = fakeBrandRepository.save(new Brand("브랜드명", "브랜드 설명", "http://brand.url"));
            Product product = fakeProductRepository.save(new Product(brand.getId(), "상품명", "상품 설명", 2000, 10, "http://product.url"));
            orderFacade.createOrder(USER_ID, List.of(new OrderCreateItem(product.getId(), 1)));
            orderFacade.createOrder(USER_ID, List.of(new OrderCreateItem(product.getId(), 1)));

            // act
            List<OrderInfo> results = orderFacade.getOrdersByUserId(USER_ID);

            // assert
            assertThat(results).hasSize(2);
        }
    }
}
