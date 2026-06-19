package com.wondealer.controller;

import com.wondealer.dto.websocket.ChatBroadcastDto;
import com.wondealer.dto.websocket.ChatRequestDto;
import com.wondealer.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * 채팅 메시지 수신 및 브로드캐스트
     * 클라이언트: SEND /app/chat/{chatRoomId}
     * 브로드캐스트: /topic/chat/{chatRoomId}
     */
    @MessageMapping("/chat/{chatRoomId}")
    @SendTo("/topic/chat/{chatRoomId}")
    public ChatBroadcastDto handleChatMessage(
            @Header("Authorization") String token,
            @DestinationVariable Long chatRoomId,
            @Payload ChatRequestDto dto
    ) {
        return chatService.handleMessage(token, chatRoomId, dto);
    }
}