package com.wondealer.dto.response;

import com.wondealer.entity.Item;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ItemDetailResDto {

    private Long itemId;
    private String title;
    private String description;
    private Long basePrice;
    private String tradeType;
    private String status;
    private int viewCount;

    private String gameName;
    private String serverName;
    private String categoryName;

    private List<String> images;

    private Long sellerId;
    private String sellerNickname;
    private String sellerProfileImg;

    private LocalDateTime createdAt;

    public static ItemDetailResDto from(Item item) {
        return ItemDetailResDto.builder()
                .itemId(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .basePrice(item.getPrice())
                .tradeType(item.getTradeType().name())
                .status(item.getStatus().name())
                .viewCount(item.getViewCount())
                .gameName(item.getGameCategory().getGame().getGameName())
                .serverName(item.getGameServer() == null ? null : item.getGameServer().getServerName())
                .categoryName(item.getGameCategory().getCategoryName())
                .images(item.getImages().stream()
                        .map(image -> image.getImageUrl())
                        .toList())
                .sellerId(item.getSeller().getId())
                .sellerNickname(item.getSeller().getNickname())
                .sellerProfileImg(item.getSeller().getProfileImg())
                .createdAt(item.getCreatedAt())
                .build();
    }
}