package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponFacade;
import com.loopers.application.coupon.CouponInfo;
import com.loopers.application.coupon.UserCouponInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/coupons")
public class CouponAdminV1Controller implements CouponAdminV1ApiSpec {

    private static final String ADMIN_LDAP_HEADER = "X-Loopers-Ldap";
    private static final String ADMIN_LDAP_VALUE = "loopers.admin";

    private final CouponFacade couponFacade;

    private void validateAdmin(String ldap) {
        if (!ADMIN_LDAP_VALUE.equals(ldap)) {
            throw new CoreException(ErrorType.FORBIDDEN, "어드민 권한이 필요합니다.");
        }
    }

    @GetMapping
    @Override
    public ApiResponse<CouponAdminV1Dto.CouponListResponse> getAllCoupons(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap
    ) {
        validateAdmin(ldap);
        List<CouponInfo> infos = couponFacade.getAllCoupons();
        return ApiResponse.success(CouponAdminV1Dto.CouponListResponse.from(infos));
    }

    @GetMapping("/{couponId}")
    @Override
    public ApiResponse<CouponAdminV1Dto.CouponResponse> getCoupon(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @PathVariable Long couponId
    ) {
        validateAdmin(ldap);
        CouponInfo info = couponFacade.getCoupon(couponId);
        return ApiResponse.success(CouponAdminV1Dto.CouponResponse.from(info));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<CouponAdminV1Dto.CouponResponse> registerCoupon(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @Valid @RequestBody CouponAdminV1Dto.RegisterRequest request
    ) {
        validateAdmin(ldap);
        CouponInfo info = couponFacade.registerCoupon(
            request.name(),
            request.type(),
            request.value(),
            request.minOrderAmount(),
            request.validDays(),
            request.totalLimit()
        );
        return ApiResponse.success(CouponAdminV1Dto.CouponResponse.from(info));
    }

    @PutMapping("/{couponId}")
    @Override
    public ApiResponse<CouponAdminV1Dto.CouponResponse> updateCoupon(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @PathVariable Long couponId,
        @Valid @RequestBody CouponAdminV1Dto.UpdateRequest request
    ) {
        validateAdmin(ldap);
        CouponInfo info = couponFacade.updateCoupon(couponId, request.name(), request.minOrderAmount());
        return ApiResponse.success(CouponAdminV1Dto.CouponResponse.from(info));
    }

    @DeleteMapping("/{couponId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public ApiResponse<Void> deleteCoupon(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @PathVariable Long couponId
    ) {
        validateAdmin(ldap);
        couponFacade.deleteCoupon(couponId);
        return ApiResponse.success(null);
    }

    @GetMapping("/{couponId}/issues")
    @Override
    public ApiResponse<CouponAdminV1Dto.IssuedCouponListResponse> getIssuedCoupons(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @PathVariable Long couponId
    ) {
        validateAdmin(ldap);
        List<UserCouponInfo> infos = couponFacade.getIssuedCoupons(couponId);
        return ApiResponse.success(CouponAdminV1Dto.IssuedCouponListResponse.from(infos));
    }
}
