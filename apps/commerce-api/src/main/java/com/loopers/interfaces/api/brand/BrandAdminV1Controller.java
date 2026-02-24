package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandFacade;
import com.loopers.application.brand.BrandInfo;
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
@RequestMapping("/api-admin/v1/brands")
public class BrandAdminV1Controller implements BrandAdminV1ApiSpec {

    private static final String ADMIN_LDAP_HEADER = "X-Loopers-Ldap";
    private static final String ADMIN_LDAP_VALUE = "loopers.admin";

    private final BrandFacade brandFacade;

    private void validateAdmin(String ldap) {
        if (!ADMIN_LDAP_VALUE.equals(ldap)) {
            throw new CoreException(ErrorType.FORBIDDEN, "어드민 권한이 필요합니다.");
        }
    }

    @GetMapping
    @Override
    public ApiResponse<BrandAdminV1Dto.BrandListResponse> getAllBrands(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap
    ) {
        validateAdmin(ldap);
        List<BrandInfo> infos = brandFacade.getAllBrands();
        return ApiResponse.success(BrandAdminV1Dto.BrandListResponse.from(infos));
    }

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<BrandAdminV1Dto.BrandResponse> getBrand(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @PathVariable Long brandId
    ) {
        validateAdmin(ldap);
        BrandInfo info = brandFacade.getBrand(brandId);
        return ApiResponse.success(BrandAdminV1Dto.BrandResponse.from(info));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<BrandAdminV1Dto.BrandResponse> registerBrand(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @Valid @RequestBody BrandAdminV1Dto.RegisterRequest request
    ) {
        validateAdmin(ldap);
        BrandInfo info = brandFacade.register(request.name(), request.description(), request.imageUrl());
        return ApiResponse.success(BrandAdminV1Dto.BrandResponse.from(info));
    }

    @PutMapping("/{brandId}")
    @Override
    public ApiResponse<BrandAdminV1Dto.BrandResponse> updateBrand(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @PathVariable Long brandId,
        @Valid @RequestBody BrandAdminV1Dto.UpdateRequest request
    ) {
        validateAdmin(ldap);
        BrandInfo info = brandFacade.update(brandId, request.name(), request.description(), request.imageUrl());
        return ApiResponse.success(BrandAdminV1Dto.BrandResponse.from(info));
    }

    @DeleteMapping("/{brandId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public ApiResponse<Void> deleteBrand(
        @RequestHeader(ADMIN_LDAP_HEADER) String ldap,
        @PathVariable Long brandId
    ) {
        validateAdmin(ldap);
        brandFacade.delete(brandId);
        return ApiResponse.success(null);
    }
}
