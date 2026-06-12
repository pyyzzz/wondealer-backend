package com.wondealer.dto.response;

import com.wondealer.entity.Item;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ItemListResDto {

    private Long itemId;
    private String title;
    private Long basePrice;
    private String tradeType;
    private String status;
    private String gameName;
    private String serverName;
    private String categoryName;
    private String thumbnailImg;
    private int viewCount;
    private LocalDateTime createdAt;

    public static ItemListResDto from(Item item) {
        String thumbnailImg = item.getImages().isEmpty()
                ? null
                : item.getImages().get(0).getImageUrl();

        return ItemListResDto.builder()
                .itemId(item.getId())
                .title(item.getTitle())
                .basePrice(item.getPrice())
                .tradeType(item.getTradeType().name())
                .status(item.getStatus().name())
                .gameName(item.getGameCategory().getGame().getGameName())
                .serverName(item.getGameServer() == null ? null : item.getGameServer().getServerName())
                .categoryName(item.getGameCategory().getCategoryName())
                .thumbnailImg(thumbnailImg)
                .viewCount(item.getViewCount())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
