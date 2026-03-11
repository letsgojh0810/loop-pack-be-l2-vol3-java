package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public enum ProductSort {
    LATEST,
    PRICE_ASC,
    LIKES_DESC;

    public static ProductSort from(String value) {
        return switch (value.toLowerCase()) {
            case "latest" -> LATEST;
            case "price_asc" -> PRICE_ASC;
            case "likes_desc" -> LIKES_DESC;
            default -> throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 정렬 기준입니다: " + value);
        };
    }
}
