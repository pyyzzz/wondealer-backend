package com.wondealer.config;

import com.wondealer.entity.*;
import com.wondealer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TermsRepository termsRepository;
    private final GameRepository gameRepository;
    private final GameCategoryRepository gameCategoryRepository;
    private final GameServerRepository gameServerRepository;

    @Override
    public void run(String... args) throws Exception {
        initTerms();
        initGames();
    }

    // ── 약관 초기 데이터 ──────────────────────────────────────────
    private void initTerms() {
        if (termsRepository.count() > 0) return;

        termsRepository.save(Terms.builder()
                .title("서비스 이용약관")
                .content("제1조 (목적)")
                .isRequired(true)
                .version("v1.0")
                .build());

        termsRepository.save(Terms.builder()
                .title("개인정보 처리방침")
                .content("제1조 (개인정보의 처리 목적)")
                .isRequired(true)
                .version("v1.0")
                .build());

        log.info("약관 초기 데이터 삽입 완료");
    }

    // ── 게임/카테고리/서버 초기 데이터 ────────────────────────────
    private void initGames() {
        if (gameRepository.count() > 0) return;

        // ── 로스트아크 ──────────────────────────────────────────
        Game lostArk = gameRepository.save(Game.builder()
                .gameName("로스트아크")
                .isActive(true)
                .build());
        saveCategories(lostArk, true);  // 아이템/게임머니/계정/기타 전부
        saveServers(lostArk, List.of("아브렐슈드", "카단", "니나브", "루페온"));

        // ── 메이플스토리 ────────────────────────────────────────
        Game maple = gameRepository.save(Game.builder()
                .gameName("메이플스토리")
                .isActive(true)
                .build());
        saveCategories(maple, true);
        saveServers(maple, List.of("리부트", "일반"));

        // ── 디아블로4 ───────────────────────────────────────────
        Game diablo = gameRepository.save(Game.builder()
                .gameName("디아블로4")
                .isActive(true)
                .build());
        saveCategories(diablo, true);
        // 서버 없음

        // ── 리그 오브 레전드 ────────────────────────────────────
        // 아이템/게임머니 거래 불가 → 계정/기타만
        Game lol = gameRepository.save(Game.builder()
                .gameName("리그 오브 레전드")
                .isActive(true)
                .build());
        saveCategories(lol, false);
        // 서버 없음

        // ── 발로란트 ────────────────────────────────────────────
        // 아이템/게임머니 거래 불가 → 계정/기타만
        Game valorant = gameRepository.save(Game.builder()
                .gameName("발로란트")
                .isActive(true)
                .build());
        saveCategories(valorant, false);
        // 서버 없음

        log.info("게임/카테고리/서버 초기 데이터 삽입 완료");
    }

    /**
     * 게임 카테고리 저장
     * @param game 대상 게임
     * @param full true: 아이템/게임머니/계정/기타 / false: 계정/기타만
     */
    private void saveCategories(Game game, boolean full) {
        if (full) {
            gameCategoryRepository.save(GameCategory.builder().game(game).categoryName("아이템").build());
            gameCategoryRepository.save(GameCategory.builder().game(game).categoryName("게임머니").build());
        }
        gameCategoryRepository.save(GameCategory.builder().game(game).categoryName("계정").build());
        gameCategoryRepository.save(GameCategory.builder().game(game).categoryName("기타").build());
    }

    /**
     * 게임 서버 저장
     */
    private void saveServers(Game game, List<String> serverNames) {
        for (String name : serverNames) {
            gameServerRepository.save(GameServer.builder()
                    .game(game)
                    .serverName(name)
                    .isActive(true)
                    .build());
        }
    }
}
