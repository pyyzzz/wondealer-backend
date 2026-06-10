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

    @Transactional
    public void crawlAndSave() {
        List<GameRanking> rankings = gameRankingCrawler.crawl();

        if (rankings.isEmpty()) {
            log.warn("크롤링 결과 없음 - 기존 DB 데이터 유지");
            return;
        }

        gameRankingRepository.deleteAllInBatch();
        gameRankingRepository.saveAll(rankings);
        log.info("게임 순위 업데이트 완료 - {}개", rankings.size());
    }

    public List<GameRankingResDto> getRankings() {
        return gameRankingRepository.findAllByOrderByGameRankAsc()
                .stream()
                .map(GameRankingResDto::of)
                .collect(Collectors.toList());
    }
}
