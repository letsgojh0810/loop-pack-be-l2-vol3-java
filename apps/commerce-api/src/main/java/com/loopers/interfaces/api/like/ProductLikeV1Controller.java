package com.loopers.interfaces.api.like;

import com.loopers.application.like.ProductLikeFacade;
import com.loopers.application.like.ProductLikeInfo;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class ProductLikeV1Controller implements ProductLikeV1ApiSpec {

    private static final String HEADER_LOGIN_ID = "X-Loopers-LoginId";
    private static final String HEADER_LOGIN_PW = "X-Loopers-LoginPw";

    private final UserFacade userFacade;
    private final ProductLikeFacade productLikeFacade;

    @PostMapping("/products/{productId}/likes")
    @Override
    public ApiResponse<ProductLikeV1Dto.LikeResponse> like(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password,
        @PathVariable Long productId
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);
        productLikeFacade.like(currentUser.id(), productId);
        ProductLikeInfo info = productLikeFacade.getLikeInfo(currentUser.id(), productId);
        return ApiResponse.success(ProductLikeV1Dto.LikeResponse.from(info));
    }

    @DeleteMapping("/products/{productId}/likes")
    @Override
    public ApiResponse<ProductLikeV1Dto.LikeResponse> unlike(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password,
        @PathVariable Long productId
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);
        productLikeFacade.unlike(currentUser.id(), productId);
        ProductLikeInfo info = productLikeFacade.getLikeInfo(currentUser.id(), productId);
        return ApiResponse.success(ProductLikeV1Dto.LikeResponse.from(info));
    }

    @GetMapping("/users/{userId}/likes")
    @Override
    public ApiResponse<ProductLikeV1Dto.LikedProductListResponse> getLikedProducts(
        @RequestHeader(HEADER_LOGIN_ID) String loginId,
        @RequestHeader(HEADER_LOGIN_PW) String password,
        @PathVariable Long userId
    ) {
        UserInfo currentUser = userFacade.getMe(loginId, password);
        if (!currentUser.id().equals(userId)) {
            throw new CoreException(ErrorType.FORBIDDEN, "본인의 좋아요 목록만 조회할 수 있습니다.");
        }
        List<ProductInfo> infos = productLikeFacade.getLikedProducts(userId);
        return ApiResponse.success(ProductLikeV1Dto.LikedProductListResponse.from(infos));
    }
}
