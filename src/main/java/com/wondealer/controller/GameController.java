package com.wondealer.controller;

import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.GameCategoryResDto;
import com.wondealer.dto.response.GameResDto;
import com.wondealer.dto.response.GameServerResDto;
import com.wondealer.repository.GameCategoryRepository;
import com.wondealer.repository.GameRepository;
import com.wondealer.repository.GameServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameRepository gameRepository;
    private final GameCategoryRepository gameCategoryRepository;
    private final GameServerRepository gameServerRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GameResDto>>> getGames() {
        List<GameResDto> response = gameRepository.findByIsActiveTrue().stream()
                .map(GameResDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{gameId}/categories")
    public ResponseEntity<ApiResponse<List<GameCategoryResDto>>> getCategories(
            @PathVariable Long gameId
    ) {
        List<GameCategoryResDto> response = gameCategoryRepository.findByGameId(gameId).stream()
                .map(GameCategoryResDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{gameId}/servers")
    public ResponseEntity<ApiResponse<List<GameServerResDto>>> getServers(
            @PathVariable Long gameId
    ) {
        List<GameServerResDto> response = gameServerRepository.findByGameIdAndIsActiveTrue(gameId).stream()
                .map(GameServerResDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
