package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "products",
    indexes = {
        // 전체 조회 정렬 인덱스
        @Index(name = "idx_products_created_at",  columnList = "created_at DESC"),
        @Index(name = "idx_products_price",       columnList = "price ASC"),
        @Index(name = "idx_products_like_count",  columnList = "like_count DESC"),
        // 브랜드 필터 + 정렬 복합 인덱스 (brand_id 선행 컬럼으로 range scan 후 정렬)
        @Index(name = "idx_products_brand_created_at", columnList = "brand_id, created_at DESC"),
        @Index(name = "idx_products_brand_price",      columnList = "brand_id, price ASC"),
        @Index(name = "idx_products_brand_like_count", columnList = "brand_id, like_count DESC")
    }
)
public class Product extends BaseEntity {

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "like_count", nullable = false)
    private long likeCount = 0;

    protected Product() {}

    public Product(Long brandId, String name, String description, int price, int stock, String imageUrl) {
        validateBrandId(brandId);
        validateName(name);
        validatePrice(price);
        validateStock(stock);

        this.brandId = brandId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        this.stock -= quantity;
    }

    public boolean hasEnoughStock(int quantity) {
        return this.stock >= quantity;
    }

    public void update(String name, String description, int price, int stock, String imageUrl) {
        validateName(name);
        validatePrice(price);
        validateStock(stock);

        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    private void validateBrandId(Long brandId) {
        if (brandId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 ID는 비어있을 수 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품명은 비어있을 수 없습니다.");
        }
    }

    private void validatePrice(int price) {
        if (price < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "가격은 0 이상이어야 합니다.");
        }
    }

    private void validateStock(int stock) {
        if (stock < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고는 0 이상이어야 합니다.");
        }
    }

    public Long getBrandId() {
        return brandId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getLikeCount() {
        return likeCount;
    }
}
