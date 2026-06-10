package com.wondealer.scheduler;

import com.wondealer.service.GameRankingService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 게임 순위 크롤링 스케줄러
 *
 * @PostConstruct  → 서버 시작 시 1회 즉시 실행
 * @Scheduled      → 이후 매 1시간마다 실행
 *
 * ⚠️ @EnableScheduling 이 WondealerBackendApplication.java에 추가되어야 함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameRankingScheduler {

    private final GameRankingService gameRankingService;

    // 서버 시작 시 1회 즉시 실행
    @PostConstruct
    public void initCrawl() {
        log.info("서버 시작 - 게임 순위 초기 크롤링 시작");
        gameRankingService.crawlAndSave();
    }

    // 이후 매 1시간마다 실행 (3600000ms = 1시간)
    // fixedDelay: 이전 작업 완료 후 1시간 뒤 실행 (중복 실행 방지)
    @Scheduled(fixedDelay = 3600000)
    public void scheduledCrawl() {
        log.info("스케줄링 - 게임 순위 크롤링 시작");
        gameRankingService.crawlAndSave();
    }
}
