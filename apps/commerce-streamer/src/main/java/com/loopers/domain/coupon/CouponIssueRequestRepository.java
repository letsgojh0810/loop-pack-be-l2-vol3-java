package com.loopers.domain.coupon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponIssueRequestRepository extends JpaRepository<CouponIssueRequest, Long> {
    Optional<CouponIssueRequest> findByRequestId(String requestId);
}
