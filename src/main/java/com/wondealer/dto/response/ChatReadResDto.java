package com.wondealer.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatReadResDto {

    private int updatedCount;

    public static ChatReadResDto of(int updatedCount) {
        return ChatReadResDto.builder()
                .updatedCount(updatedCount)
                .build();
    }
}