package com.loopers.domain.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;

@Entity
@Table(name = "coupon_issue_requests")
public class CouponIssueRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String requestId;

    @Column(nullable = false)
    private Long couponId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponIssueRequestStatus status;

    @Column
    private String failReason;

    @Column
    private ZonedDateTime processedAt;

    protected CouponIssueRequest() {}

    public void success() {
        this.status = CouponIssueRequestStatus.SUCCESS;
        this.processedAt = ZonedDateTime.now();
    }

    public void fail(String reason) {
        this.status = CouponIssueRequestStatus.FAILED;
        this.failReason = reason;
        this.processedAt = ZonedDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public Long getUserId() {
        return userId;
    }

    public CouponIssueRequestStatus getStatus() {
        return status;
    }

    public String getFailReason() {
        return failReason;
    }
}
