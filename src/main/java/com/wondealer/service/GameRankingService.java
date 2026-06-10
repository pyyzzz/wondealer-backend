package com.wondealer.service;

import com.wondealer.crawler.GameRankingCrawler;
import com.wondealer.dto.response.GameRankingResDto;
import com.wondealer.entity.GameRanking;
import com.wondealer.repository.GameRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameRankingService {

    private final GameRankingRepository gameRankingRepository;
    private final GameRankingCrawler gameRankingCrawler;

    /**
     * 크롤링 실행 후 DB 저장
     * 성공: 기존 전체 삭제 → 새 데이터 저장
     * 실패: 빈 리스트 반환 → 기존 데이터 유지 (fallback)
     */
    @Transactional
    public void crawlAndSave() {
        List<GameRanking> rankings = gameRankingCrawler.crawl();

        // 크롤링 실패 시 빈 리스트 반환 → 기존 데이터 유지
        if (rankings.isEmpty()) {
            log.warn("크롤링 결과 없음 - 기존 DB 데이터 유지");
            return;
        }

        // 기존 데이터 전체 삭제 후 새 데이터 저장 (순위 교체)
        gameRankingRepository.deleteAllInBatch();
        gameRankingRepository.saveAll(rankings);
        log.info("게임 순위 업데이트 완료 - {}개", rankings.size());
    }

    /**
     * 저장된 게임 순위 조회 (메인 페이지용)
     */
    public List<GameRankingResDto> getRankings() {
        return gameRankingRepository.findAllByOrderByRankAsc()
                .stream()
                .map(GameRankingResDto::of)
                .collect(Collectors.toList());
    }
}
