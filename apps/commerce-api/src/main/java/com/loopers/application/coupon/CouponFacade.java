package com.loopers.application.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.confg.kafka.message.CouponIssueRequestMessage;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponIssueRequest;
import com.loopers.domain.coupon.CouponIssueRequestRepository;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Component
public class CouponFacade {

    private static final String COUPON_ISSUE_TOPIC = "coupon-issue-requests";

    private final CouponService couponService;
    private final CouponIssueRequestRepository couponIssueRequestRepository;
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final ObjectMapper objectMapper;

    public CouponFacade(
            CouponService couponService,
            CouponIssueRequestRepository couponIssueRequestRepository,
            @Qualifier("stringKafkaTemplate") KafkaTemplate<String, String> stringKafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.couponService = couponService;
        this.couponIssueRequestRepository = couponIssueRequestRepository;
        this.stringKafkaTemplate = stringKafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public UserCouponInfo issueCoupon(Long couponId, Long userId) {
        UserCoupon userCoupon = couponService.issueCoupon(couponId, userId);
        Coupon coupon = couponService.getCoupon(userCoupon.getCouponId());
        return UserCouponInfo.from(userCoupon, coupon);
    }

    public List<UserCouponInfo> getMyUserCoupons(Long userId) {
        List<UserCoupon> userCoupons = couponService.getUserCoupons(userId);
        return userCoupons.stream()
            .map(userCoupon -> {
                Coupon coupon = couponService.getCoupon(userCoupon.getCouponId());
                return UserCouponInfo.from(userCoupon, coupon);
            })
            .toList();
    }

    public CouponInfo registerCoupon(String name, CouponType type, int value, Integer minOrderAmount, int validDays, Integer totalLimit) {
        Coupon coupon = couponService.registerCoupon(name, type, value, minOrderAmount, validDays, totalLimit);
        return CouponInfo.from(coupon);
    }

    public List<CouponInfo> getAllCoupons() {
        return couponService.getAllCoupons().stream()
            .map(CouponInfo::from)
            .toList();
    }

    public CouponInfo getCoupon(Long couponId) {
        Coupon coupon = couponService.getCoupon(couponId);
        return CouponInfo.from(coupon);
    }

    public List<UserCouponInfo> getIssuedCoupons(Long couponId) {
        List<UserCoupon> userCoupons = couponService.getIssuedCoupons(couponId);
        Coupon coupon = couponService.getCoupon(couponId);
        return userCoupons.stream()
            .map(userCoupon -> UserCouponInfo.from(userCoupon, coupon))
            .toList();
    }

    public CouponInfo updateCoupon(Long couponId, String name, Integer minOrderAmount) {
        Coupon coupon = couponService.updateCoupon(couponId, name, minOrderAmount);
        return CouponInfo.from(coupon);
    }

    public void deleteCoupon(Long couponId) {
        couponService.deleteCoupon(couponId);
    }

    @Transactional
    public CouponIssueRequestInfo requestCouponIssue(Long couponId, Long userId) {
        // 쿠폰 존재 확인
        couponService.getCoupon(couponId);

        String requestId = UUID.randomUUID().toString();
        CouponIssueRequest request = CouponIssueRequest.create(requestId, couponId, userId);
        couponIssueRequestRepository.save(request);

        CouponIssueRequestMessage message = new CouponIssueRequestMessage(
                requestId,
                couponId,
                userId,
                Instant.now().toEpochMilli()
        );

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("쿠폰 발급 요청 직렬화 실패: " + e.getMessage(), e);
        }

        try {
            stringKafkaTemplate.send(COUPON_ISSUE_TOPIC, couponId.toString(), json).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kafka 발행 중 인터럽트 발생", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Kafka 발행 실패: " + e.getMessage(), e);
        }

        return CouponIssueRequestInfo.from(request);
    }

    public CouponIssueRequestInfo getIssueRequestStatus(String requestId) {
        CouponIssueRequest request = couponIssueRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰 발급 요청을 찾을 수 없습니다."));
        return CouponIssueRequestInfo.from(request);
    }
}
