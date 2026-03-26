package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderFacade {

    private final ProductService productService;
    private final BrandService brandService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderInfo createOrder(Long userId, List<OrderCreateItem> items, Long couponId) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderCreateItem createItem : items) {
            Product product = productService.getProduct(createItem.productId());

            if (!product.hasEnoughStock(createItem.quantity())) {
                throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
            }

            productService.decreaseStock(product.getId(), createItem.quantity());

            Brand brand = brandService.getBrand(product.getBrandId());

            OrderItem orderItem = OrderItem.createSnapshot(
                product.getId(),
                brand.getName(),
                product.getName(),
                product.getPrice(),
                createItem.quantity()
            );
            orderItems.add(orderItem);
        }

        int originalAmount = orderItems.stream()
            .mapToInt(item -> item.getPrice() * item.getQuantity())
            .sum();

        Order order;
        if (couponId != null) {
            int discountAmount = couponService.validateAndUse(couponId, userId, originalAmount);
            order = orderService.createOrder(userId, orderItems, discountAmount, couponId);
        } else {
            order = orderService.createOrder(userId, orderItems);
        }

        List<OrderItem> savedItems = orderService.getOrderItems(order.getId());
        List<OrderItemInfo> itemInfos = savedItems.stream()
            .map(OrderItemInfo::from)
            .toList();

        OrderInfo orderInfo = OrderInfo.of(order, itemInfos);
        eventPublisher.publishEvent(new OrderCreatedEvent(order.getId(), userId));
        return orderInfo;
    }

    public OrderInfo getOrder(Long orderId) {
        Order order = orderService.getOrder(orderId);
        List<OrderItem> items = orderService.getOrderItems(orderId);
        List<OrderItemInfo> itemInfos = items.stream()
            .map(OrderItemInfo::from)
            .toList();
        return OrderInfo.of(order, itemInfos);
    }

    public List<OrderInfo> getOrdersByUserId(Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return orders.stream()
            .map(order -> {
                List<OrderItem> items = orderService.getOrderItems(order.getId());
                List<OrderItemInfo> itemInfos = items.stream()
                    .map(OrderItemInfo::from)
                    .toList();
                return OrderInfo.of(order, itemInfos);
            })
            .toList();
    }

    public List<OrderInfo> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return orders.stream()
            .map(order -> {
                List<OrderItem> items = orderService.getOrderItems(order.getId());
                List<OrderItemInfo> itemInfos = items.stream()
                    .map(OrderItemInfo::from)
                    .toList();
                return OrderInfo.of(order, itemInfos);
            })
            .toList();
    }
}
