package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "coupons")
public class Coupon extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CouponType type;

    @Column(name = "value", nullable = false)
    private int value;

    @Column(name = "min_order_amount")
    private Integer minOrderAmount;

    @Column(name = "valid_days", nullable = false)
    private int validDays;

    @Column(name = "total_limit")
    private Integer totalLimit;

    protected Coupon() {}

    public Coupon(String name, CouponType type, int value, Integer minOrderAmount, int validDays) {
        this(name, type, value, minOrderAmount, validDays, null);
    }

    public Coupon(String name, CouponType type, int value, Integer minOrderAmount, int validDays, Integer totalLimit) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 이름은 비어있을 수 없습니다.");
        }
        if (type == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 타입은 비어있을 수 없습니다.");
        }
        if (value <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 할인 값은 0보다 커야 합니다.");
        }
        if (validDays <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효 기간은 0보다 커야 합니다.");
        }
        this.name = name;
        this.type = type;
        this.value = value;
        this.minOrderAmount = minOrderAmount;
        this.validDays = validDays;
        this.totalLimit = totalLimit;
    }

    public void update(String name, Integer minOrderAmount) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 이름은 비어있을 수 없습니다.");
        }
        this.name = name;
        this.minOrderAmount = minOrderAmount;
    }

    public void validateMinOrderAmount(int amount) {
        if (minOrderAmount != null && amount < minOrderAmount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "최소 주문 금액을 충족하지 않습니다.");
        }
    }

    public int calculateDiscount(int originalAmount) {
        if (type == CouponType.FIXED) {
            return value;
        }
        return originalAmount * value / 100;
    }

    public String getName() {
        return name;
    }

    public CouponType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public Integer getMinOrderAmount() {
        return minOrderAmount;
    }

    public int getValidDays() {
        return validDays;
    }

    public Integer getTotalLimit() {
        return totalLimit;
    }
}
