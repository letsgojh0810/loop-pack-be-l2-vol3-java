package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/products")
public class ProductAdminV1Controller implements ProductAdminV1ApiSpec {

    private static final String ADMIN_LDAP_HEADER = "X-Loopers-Ldap";
    private static final String ADMIN_LDAP_VALUE = "loopers.admin";

    private final ProductFacade productFacade;

    private void validateAdmin(String ldap) {
        if (!ADMIN_LDAP_VALUE.equals(ldap)) {
            throw new CoreException(ErrorType.FORBIDDEN, "어드민 권한이 필요합니다.");
        }
    }

    @GetMapping
    @Override
    public ApiResponse<ProductAdminV1Dto.ProductListResponse> getProducts(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @RequestParam(required = false) Long brandId
    ) {
        validateAdmin(ldap);
        List<ProductInfo> infos = productFacade.getProducts(brandId);
        return ApiResponse.success(ProductAdminV1Dto.ProductListResponse.from(infos));
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductAdminV1Dto.ProductResponse> getProductDetail(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @PathVariable Long productId
    ) {
        validateAdmin(ldap);
        ProductInfo info = productFacade.getProductDetail(productId, null);
        return ApiResponse.success(ProductAdminV1Dto.ProductResponse.from(info));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<ProductAdminV1Dto.ProductResponse> registerProduct(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @Valid @RequestBody ProductAdminV1Dto.RegisterRequest request
    ) {
        validateAdmin(ldap);
        ProductInfo info = productFacade.registerProduct(
            request.brandId(),
            request.name(),
            request.description(),
            request.price(),
            request.stock(),
            request.imageUrl()
        );
        return ApiResponse.success(ProductAdminV1Dto.ProductResponse.from(info));
    }

    @PutMapping("/{productId}")
    @Override
    public ApiResponse<ProductAdminV1Dto.ProductResponse> updateProduct(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @PathVariable Long productId,
        @Valid @RequestBody ProductAdminV1Dto.UpdateRequest request
    ) {
        validateAdmin(ldap);
        ProductInfo info = productFacade.updateProduct(
            productId,
            request.name(),
            request.description(),
            request.price(),
            request.stock(),
            request.imageUrl()
        );
        return ApiResponse.success(ProductAdminV1Dto.ProductResponse.from(info));
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public ApiResponse<Void> deleteProduct(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @PathVariable Long productId
    ) {
        validateAdmin(ldap);
        productFacade.deleteProduct(productId);
        return ApiResponse.success(null);
    }
}
