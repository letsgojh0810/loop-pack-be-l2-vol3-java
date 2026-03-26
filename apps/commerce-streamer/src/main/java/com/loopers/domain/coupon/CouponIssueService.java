package com.loopers.domain.coupon;

import com.loopers.confg.kafka.message.CouponIssueRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponIssueService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponIssueRequestRepository couponIssueRequestRepository;

    @Transactional
    public void processIssue(CouponIssueRequestMessage msg) {
        CouponIssueRequest request = couponIssueRequestRepository.findByRequestId(msg.requestId())
                .orElse(null);
        if (request == null) {
            log.warn("쿠폰 발급 요청을 찾을 수 없습니다: requestId={}", msg.requestId());
            return;
        }

        try {
            Coupon coupon = couponRepository.findByIdForUpdate(msg.couponId())
                    .orElseThrow(() -> new IllegalStateException("쿠폰을 찾을 수 없습니다: couponId=" + msg.couponId()));

            // 수량 체크
            if (coupon.getTotalLimit() != null) {
                long issuedCount = userCouponRepository.countByCouponId(msg.couponId());
                if (issuedCount >= coupon.getTotalLimit()) {
                    request.fail("선착순 마감");
                    return;
                }
            }

            // 중복 체크
            if (userCouponRepository.existsByUserIdAndCouponId(msg.userId(), msg.couponId())) {
                request.fail("이미 발급받은 쿠폰");
                return;
            }

            // 발급
            UserCoupon userCoupon = UserCoupon.issue(coupon, msg.userId());
            userCouponRepository.save(userCoupon);
            request.success();

        } catch (Exception e) {
            log.error("쿠폰 발급 처리 중 오류: requestId={}", msg.requestId(), e);
            request.fail(e.getMessage());
        }
    }
}
