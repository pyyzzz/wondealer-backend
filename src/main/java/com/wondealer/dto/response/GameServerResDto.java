package com.wondealer.dto.response;

import com.wondealer.entity.GameServer;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameServerResDto {

    private Long serverId;
    private String serverName;

    public static GameServerResDto from(GameServer server) {
        return GameServerResDto.builder()
                .serverId(server.getId())
                .serverName(server.getServerName())
                .build();
    }
}
