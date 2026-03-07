package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons")
public class UserCoupon extends BaseEntity {

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CouponStatus status;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    protected UserCoupon() {}

    private UserCoupon(Long couponId, Long userId, CouponStatus status, LocalDateTime issuedAt, LocalDateTime expiredAt) {
        this.couponId = couponId;
        this.userId = userId;
        this.status = status;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;
    }

    public static UserCoupon issue(Coupon coupon, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusDays(coupon.getValidDays());
        return new UserCoupon(coupon.getId(), userId, CouponStatus.AVAILABLE, now, expiredAt);
    }

    public void validate(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "본인의 쿠폰만 사용할 수 있습니다.");
        }
        if (this.status == CouponStatus.USED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }
        if (this.status == CouponStatus.EXPIRED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "만료된 쿠폰입니다.");
        }
        if (LocalDateTime.now().isAfter(this.expiredAt)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "만료된 쿠폰입니다.");
        }
    }

    public void use() {
        this.status = CouponStatus.USED;
    }

    public Long getCouponId() {
        return couponId;
    }

    public Long getUserId() {
        return userId;
    }

    public CouponStatus getStatus() {
        return status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }
}
