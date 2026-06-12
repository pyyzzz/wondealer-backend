package com.wondealer.dto.response;

import com.wondealer.entity.Game;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameResDto {

    private Long gameId;
    private String gameName;
    private String gameImg;

    public static GameResDto from(Game game) {
        return GameResDto.builder()
                .gameId(game.getId())
                .gameName(game.getGameName())
                .gameImg(game.getGameImg())
                .build();
    }
}
