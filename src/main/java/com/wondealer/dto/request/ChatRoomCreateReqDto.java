package com.wondealer.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ChatRoomCreateReqDto {

    @NotNull
    private Long itemId;
}