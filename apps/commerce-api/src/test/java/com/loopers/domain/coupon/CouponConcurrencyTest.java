package com.loopers.domain.coupon;

import com.loopers.infrastructure.coupon.CouponJpaRepository;
import com.loopers.infrastructure.coupon.UserCouponJpaRepository;
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
class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private UserCouponJpaRepository userCouponJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private static final Long USER_ID = 1L;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("동일한 쿠폰을 동시에 두 번 사용하면 비관적 락으로 한 번만 성공한다.")
    @Test
    void preventsDoubleCouponUse_whenConcurrentRequestsAreMade() throws InterruptedException {
        // arrange
        Coupon coupon = couponJpaRepository.save(new Coupon("할인쿠폰", CouponType.FIXED, 1000, null, 30));
        UserCoupon userCoupon = userCouponJpaRepository.save(UserCoupon.issue(coupon, USER_ID));
        Long userCouponId = userCoupon.getId();
        int originalAmount = 10000;

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // act - 2개 스레드가 동시에 같은 쿠폰 사용
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    couponService.validateAndUse(userCouponId, USER_ID, originalAmount);
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

        // assert - 1번만 성공, 쿠폰 상태 USED
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);

        UserCoupon result = userCouponJpaRepository.findById(userCouponId).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(CouponStatus.USED);
    }
}
