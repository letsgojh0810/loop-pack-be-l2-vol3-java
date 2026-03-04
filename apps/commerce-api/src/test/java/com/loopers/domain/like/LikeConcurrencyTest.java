package com.loopers.domain.like;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.like.ProductLikeJpaRepository;
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
class LikeConcurrencyTest {

    @Autowired
    private ProductLikeService productLikeService;

    @Autowired
    private ProductLikeJpaRepository productLikeJpaRepository;

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

    @DisplayName("여러 사용자가 동시에 같은 상품에 좋아요를 누르면 정확한 수만큼 등록된다.")
    @Test
    void recordsCorrectLikeCount_whenMultipleUserLikeConcurrently() throws InterruptedException {
        // arrange
        Brand brand = brandJpaRepository.save(new Brand("브랜드", "설명", "http://brand.url"));
        Product product = productJpaRepository.save(new Product(brand.getId(), "상품", "설명", 1000, 10, "http://product.url"));
        Long productId = product.getId();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // act - 10명의 서로 다른 사용자가 동시에 좋아요
        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1L;
            executor.submit(() -> {
                try {
                    productLikeService.like(userId, productId);
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

        // assert - 10개가 정상 등록
        long likeCount = productLikeJpaRepository.countByProductId(productId);
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(likeCount).isEqualTo(10);
    }
}
