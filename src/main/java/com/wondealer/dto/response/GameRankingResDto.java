package com.wondealer.dto.response;

import com.wondealer.entity.GameRanking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameRankingResDto {

    private Integer gameRank;  // 순위
    private String gameName;   // 게임명
    private String gameImg;    // 게임 이미지 URL

    public static GameRankingResDto of(GameRanking gameRanking) {
        return GameRankingResDto.builder()
                .gameRank(gameRanking.getGameRank())
                .gameName(gameRanking.getGameName())
                .gameImg(gameRanking.getGameImg())
                .build();
    }
}
