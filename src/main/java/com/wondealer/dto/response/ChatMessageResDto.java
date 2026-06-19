package com.wondealer.dto.response;

import com.wondealer.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResDto {

    private Long messageId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static ChatMessageResDto from(ChatMessage message) {
        return ChatMessageResDto.builder()
                .messageId(message.getId())
                .senderId(message.getSender().getId())
                .senderNickname(message.getSender().getNickname())
                .content(message.getContent())
                .isRead(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}