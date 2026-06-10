package com.wondealer.crawler;

import com.wondealer.entity.GameRanking;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 게임메카 게임 순위 크롤러
 *
 * 대상: https://www.gamemeca.com/ranking.php
 * 추출: 1위 ~ 10위 게임명, 이미지 URL
 *
 * HTML 구조:
 *   tr.ranking-table-rows       ← 게임 한 행
 *     span.rank                 ← 순위 텍스트 ("1", "2", ...)
 *     img.game-icon             ← 게임 이미지 (src 속성)
 *     div.game-name > a         ← 게임명 텍스트
 */
@Slf4j
@Component
public class GameRankingCrawler {

    private static final String TARGET_URL = "https://www.gamemeca.com/ranking.php";
    private static final int MAX_RANK = 10;
    private static final int TIMEOUT_MS = 10_000; // 10초 타임아웃

    /**
     * 게임메카에서 순위 데이터를 크롤링해서 반환
     * 실패 시 빈 리스트 반환 → Service에서 기존 데이터 유지 (fallback)
     */
    public List<GameRanking> crawl() {
        List<GameRanking> rankings = new ArrayList<>();

        try {
            // Jsoup으로 HTML 페이지 요청
            Document doc = Jsoup.connect(TARGET_URL)
                    .timeout(TIMEOUT_MS)
                    .userAgent("Mozilla/5.0") // 봇 차단 방지
                    .get();

            // tr.ranking-table-rows 선택자로 모든 게임 행 조회
            Elements rows = doc.select("tr.ranking-table-rows");

            int count = 0;
            for (Element row : rows) {
                if (count >= MAX_RANK) break;

                // 순위 추출: span.rank 텍스트
                String rankText = row.select("span.rank").text().trim();
                // 게임명 추출: div.game-name > a 텍스트
                String gameName = row.select("div.game-name a").text().trim();
                // 이미지 URL 추출: img.game-icon의 src 속성
                String gameImg  = row.select("img.game-icon").attr("src").trim();

                // 필수 데이터 누락 시 해당 행 스킵
                if (rankText.isEmpty() || gameName.isEmpty()) {
                    log.warn("크롤링 데이터 누락 - rank: {}, gameName: {}", rankText, gameName);
                    continue;
                }

                rankings.add(GameRanking.builder()
                        .rank(Integer.parseInt(rankText))
                        .gameName(gameName)
                        .gameImg(gameImg.isEmpty() ? null : gameImg)
                        .build());

                count++;
            }

            log.info("크롤링 성공 - {}개 게임 데이터 수집", rankings.size());

        } catch (Exception e) {
            // 네트워크 오류, 파싱 오류 등 모든 예외 처리
            // 빈 리스트 반환 → Service에서 기존 DB 데이터 유지
            log.error("크롤링 실패 - 기존 데이터 유지: {}", e.getMessage());
        }

        return rankings;
    }
}
