package com.loopers.infrastructure.scheduler;

import com.loopers.application.payment.PaymentFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentCancelScheduler {

    private final PaymentFacade paymentFacade;

    @Scheduled(fixedDelay = 60000)
    public void processCancelRequests() {
        log.info("PENDING 취소 요청 스케줄러 실행");
        paymentFacade.processPendingCancelRequests();
    }
}
