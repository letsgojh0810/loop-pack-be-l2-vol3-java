package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StockConcurrencyTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("재고가 충분할 때, 동시에 여러 스레드가 재고를 차감하면 비관적 락으로 정확히 처리된다.")
    @Test
    void decreasesStockCorrectly_whenMultipleThreadsAccessConcurrently() throws InterruptedException {
        // arrange
        Brand brand = brandJpaRepository.save(new Brand("브랜드", "설명", "http://brand.url"));
        Product product = productJpaRepository.save(new Product(brand.getId(), "상품", "설명", 1000, 10, "http://product.url"));
        Long productId = product.getId();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // act - 10개 스레드가 동시에 재고 1개씩 차감
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    productService.decreaseStock(productId, 1);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // assert - 재고가 정확히 0이 되어야 함 (lost update 없음)
        Product result = productJpaRepository.findById(productId).orElseThrow();
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(result.getStock()).isEqualTo(0);
    }

    @DisplayName("재고보다 많은 스레드가 동시에 차감을 시도하면 재고 범위 내에서만 성공한다.")
    @Test
    void failsForExcessRequests_whenRequestsExceedStock() throws InterruptedException {
        // arrange
        Brand brand = brandJpaRepository.save(new Brand("브랜드", "설명", "http://brand.url"));
        Product product = productJpaRepository.save(new Product(brand.getId(), "상품", "설명", 1000, 5, "http://product.url"));
        Long productId = product.getId();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // act - 10개 스레드가 동시에 재고 1개씩 차감 (재고는 5개)
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    productService.decreaseStock(productId, 1);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // assert - 정확히 5개만 성공, 재고 0
        Product result = productJpaRepository.findById(productId).orElseThrow();
        assertThat(successCount.get()).isEqualTo(5);
        assertThat(result.getStock()).isEqualTo(0);
    }
}
