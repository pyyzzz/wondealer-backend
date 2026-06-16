package com.wondealer.dto.websocket;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 채팅 브로드캐스트 DTO
 * ChatWebSocketController에서 @SendTo("/topic/chat/{chatRoomId}")로 전송
 */
@Getter
@Builder
public class ChatBroadcastDto {

    private Long chatRoomId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private LocalDateTime createdAt;
}
