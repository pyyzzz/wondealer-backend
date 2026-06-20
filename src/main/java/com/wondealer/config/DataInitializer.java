package com.wondealer.config;

import com.wondealer.constant.Authority;
import com.wondealer.entity.*;
import com.wondealer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.name}")
    private String adminName;

    @Override
    public void run(String... args) throws Exception {
        initAdmin();
        initTerms();
        initGames();
    }

    // ── 관리자 계정 초기 생성 ─────────────────────────────────────
    private void initAdmin() {
        if (memberRepository.existsByEmail(adminEmail)) return;

        Member admin = Member.builder()
                .email(adminEmail)
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .name(adminName)
                .nickname("관리자")
                .authority(Authority.ROLE_ADMIN)
                .isEmailVerified(true)
                .isBanned(false)
                .build();

        memberRepository.save(admin);

        // 관리자도 지갑 생성
        walletRepository.save(Wallet.builder()
                .member(admin)
                .build());

        log.info("관리자 계정 생성 완료 - email: {}", adminEmail);
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
                .build());
        saveCategories(lostArk, true);
        saveServers(lostArk, List.of("아브렐슈드", "카단", "니나브", "루페온"));

        // ── 메이플스토리 ────────────────────────────────────────
        Game maple = gameRepository.save(Game.builder()
                .gameName("메이플스토리")
                .build());
        saveCategories(maple, true);
        saveServers(maple, List.of("리부트", "일반"));

        // ── 디아블로4 ───────────────────────────────────────────
        Game diablo = gameRepository.save(Game.builder()
                .gameName("디아블로4")
                .build());
        saveCategories(diablo, true);

        // ── 리그 오브 레전드 ────────────────────────────────────
        Game lol = gameRepository.save(Game.builder()
                .gameName("리그 오브 레전드")
                .build());
        saveCategories(lol, true);

        // ── 발로란트 ────────────────────────────────────────────
        Game valorant = gameRepository.save(Game.builder()
                .gameName("발로란트")
                .build());
        saveCategories(valorant, true);

        log.info("게임/카테고리/서버 초기 데이터 삽입 완료");
    }

    private void saveCategories(Game game, boolean full) {
        if (full) {
            gameCategoryRepository.save(GameCategory.builder().game(game).categoryName("아이템").build());
            gameCategoryRepository.save(GameCategory.builder().game(game).categoryName("게임머니").build());
        }
        gameCategoryRepository.save(GameCategory.builder().game(game).categoryName("계정").build());
        gameCategoryRepository.save(GameCategory.builder().game(game).categoryName("기타").build());
    }

    private void saveServers(Game game, List<String> serverNames) {
        for (String name : serverNames) {
            gameServerRepository.save(GameServer.builder()
                    .game(game)
                    .serverName(name)
                    .build());
        }
    }
}