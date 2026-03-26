package com.loopers.domain.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "valid_days", nullable = false)
    private int validDays;

    @Column(name = "total_limit")
    private Integer totalLimit;

    protected Coupon() {}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getValidDays() {
        return validDays;
    }

    public Integer getTotalLimit() {
        return totalLimit;
    }
}
