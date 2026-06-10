package com.wondealer.controller;

import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.GameRankingResDto;
import com.wondealer.service.GameRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class GameRankingController {

    private final GameRankingService gameRankingService;

    // GET /api/rankings — 게임 순위 목록 조회 (인증 불필요 - 메인 페이지 공개)
    @GetMapping
    public ResponseEntity<ApiResponse<List<GameRankingResDto>>> getRankings() {
        return ResponseEntity.ok(ApiResponse.ok(gameRankingService.getRankings()));
    }
}
