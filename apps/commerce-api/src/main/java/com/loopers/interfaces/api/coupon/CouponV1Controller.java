package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponFacade;
import com.loopers.application.coupon.CouponIssueRequestInfo;
import com.loopers.application.coupon.UserCouponInfo;
import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/coupons")
public class CouponV1Controller implements CouponV1ApiSpec {

    private static final String HEADER_LOGIN_ID = "X-Loopers-LoginId";
    private static final String HEADER_LOGIN_PW = "X-Loopers-LoginPw";

    private final UserFacade userFacade;
    private final CouponFacade couponFacade;

    @PostMapping("/{couponId}/issue")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<CouponV1Dto.UserCouponResponse> issueCoupon(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password,
        @PathVariable Long couponId
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);
        UserCouponInfo info = couponFacade.issueCoupon(couponId, currentUser.id());
        return ApiResponse.success(CouponV1Dto.UserCouponResponse.from(info));
    }

    @GetMapping("/users/me/coupons")
    @Override
    public ApiResponse<CouponV1Dto.UserCouponListResponse> getMyUserCoupons(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);
        List<UserCouponInfo> infos = couponFacade.getMyUserCoupons(currentUser.id());
        return ApiResponse.success(CouponV1Dto.UserCouponListResponse.from(infos));
    }

    @PostMapping("/{couponId}/issue-async")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Override
    public ApiResponse<CouponV1Dto.CouponIssueRequestResponse> requestCouponIssueAsync(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password,
        @PathVariable Long couponId
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);
        CouponIssueRequestInfo info = couponFacade.requestCouponIssue(couponId, currentUser.id());
        return ApiResponse.success(CouponV1Dto.CouponIssueRequestResponse.from(info));
    }

    @GetMapping("/issue-requests/{requestId}")
    @Override
    public ApiResponse<CouponV1Dto.CouponIssueRequestResponse> getIssueRequestStatus(
        @PathVariable String requestId
    ) {
        CouponIssueRequestInfo info = couponFacade.getIssueRequestStatus(requestId);
        return ApiResponse.success(CouponV1Dto.CouponIssueRequestResponse.from(info));
    }
}
