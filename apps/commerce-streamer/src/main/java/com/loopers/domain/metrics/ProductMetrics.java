package com.loopers.domain.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.ZonedDateTime;

@Entity
@Table(name = "product_metrics")
public class ProductMetrics {

    @Id
    private Long productId;

    @Column(nullable = false)
    private long likeCount = 0;

    @Column(nullable = false)
    private long viewCount = 0;

    @Column(nullable = false)
    private long salesCount = 0;

    @Column(nullable = false)
    private long lastEventVersion = 0;

    @Version
    private long optimisticLockVersion;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    protected ProductMetrics() {}

    public static ProductMetrics create(Long productId) {
        ProductMetrics m = new ProductMetrics();
        m.productId = productId;
        m.likeCount = 0;
        m.viewCount = 0;
        m.salesCount = 0;
        m.lastEventVersion = 0;
        m.updatedAt = ZonedDateTime.now();
        return m;
    }

    public void incrementLike(int delta, long version) {
        this.likeCount += delta;
        this.lastEventVersion = version;
        this.updatedAt = ZonedDateTime.now();
    }

    public void incrementView(long version) {
        this.viewCount += 1;
        this.lastEventVersion = version;
        this.updatedAt = ZonedDateTime.now();
    }

    public void incrementSales(int quantity, long version) {
        this.salesCount += quantity;
        this.lastEventVersion = version;
        this.updatedAt = ZonedDateTime.now();
    }

    public Long getProductId() {
        return productId;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public long getViewCount() {
        return viewCount;
    }

    public long getSalesCount() {
        return salesCount;
    }

    public long getLastEventVersion() {
        return lastEventVersion;
    }

    public long getOptimisticLockVersion() {
        return optimisticLockVersion;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}
