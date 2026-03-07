package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "original_amount", nullable = false)
    private int originalAmount;

    @Column(name = "discount_amount", nullable = false)
    private int discountAmount;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {}

    private Order(Long userId, int originalAmount, int discountAmount, List<OrderItem> items) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 비어있을 수 없습니다.");
        }
        if (originalAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "총 금액은 0 이상이어야 합니다.");
        }
        this.userId = userId;
        this.originalAmount = originalAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = originalAmount - discountAmount;
        this.items = items;
    }

    public static Order create(Long userId, List<OrderItem> items) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 비어있을 수 없습니다.");
        }
        if (items == null || items.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목은 비어있을 수 없습니다.");
        }
        int originalAmount = items.stream()
            .mapToInt(item -> item.getPrice() * item.getQuantity())
            .sum();
        return new Order(userId, originalAmount, 0, items);
    }

    public static Order create(Long userId, List<OrderItem> items, int discountAmount) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 비어있을 수 없습니다.");
        }
        if (items == null || items.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목은 비어있을 수 없습니다.");
        }
        int originalAmount = items.stream()
            .mapToInt(item -> item.getPrice() * item.getQuantity())
            .sum();
        return new Order(userId, originalAmount, discountAmount, items);
    }

    public Long getUserId() {
        return userId;
    }

    public int getOriginalAmount() {
        return originalAmount;
    }

    public int getDiscountAmount() {
        return discountAmount;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
