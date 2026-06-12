package com.wondealer.dto.response;

import com.wondealer.entity.Item;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemResDto {

    private Long itemId;
    private String title;
    private String tradeType;
    private String status;

    public static ItemResDto from(Item item) {
        return ItemResDto.builder()
                .itemId(item.getId())
                .title(item.getTitle())
                .tradeType(item.getTradeType().name())
                .status(item.getStatus().name())
                .build();
    }
}