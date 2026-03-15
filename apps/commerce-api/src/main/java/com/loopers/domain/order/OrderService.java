package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(Long userId, List<OrderItem> items) {
        Order order = Order.create(userId, items);
        return orderRepository.save(order);
    }

    @Transactional
    public Order createOrder(Long userId, List<OrderItem> items, int discountAmount, Long userCouponId) {
        Order order = Order.create(userId, items, discountAmount, userCouponId);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItems(Long orderId) {
        return getOrder(orderId).getItems();
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId);
    }

    @Transactional
    public void completeOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.complete();
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.cancel();
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public void completeOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.complete();
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.cancel();
    }
}
