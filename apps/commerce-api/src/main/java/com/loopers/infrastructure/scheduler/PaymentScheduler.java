package com.loopers.infrastructure.scheduler;

import com.loopers.application.payment.PaymentFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentScheduler {

    private final PaymentFacade paymentFacade;

    @Scheduled(fixedDelay = 30000)
    public void syncPendingPayments() {
        log.info("PENDING 결제 동기화 스케줄러 실행");
        paymentFacade.syncPendingPayments();
    }
}
