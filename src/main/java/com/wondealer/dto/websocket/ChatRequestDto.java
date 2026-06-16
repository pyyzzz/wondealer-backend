package com.wondealer.dto.websocket;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * 채팅 메시지 요청 DTO
 * 클라이언트가 SEND /app/chat/{chatRoomId} 로 전송하는 메시지
 */
@Getter
public class ChatRequestDto {

    @NotBlank
    private String content;
}
