package com.loopers.interfaces.scheduler;

import com.loopers.domain.ranking.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@Component
public class RankingCarryOverScheduler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RankingService rankingService;

    @Scheduled(cron = "0 50 23 * * *")
    public void carryOver() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String tomorrow = LocalDate.now().plusDays(1).format(DATE_FORMATTER);
        try {
            rankingService.carryOver(today, tomorrow);
            log.info("랭킹 carry-over 완료: {} → {} (가중치 10%)", today, tomorrow);
        } catch (Exception e) {
            log.error("랭킹 carry-over 실패: {} → {}", today, tomorrow, e);
        }
    }
}
