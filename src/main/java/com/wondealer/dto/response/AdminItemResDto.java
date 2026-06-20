package com.wondealer.dto.response;

import com.wondealer.entity.Item;
import com.wondealer.entity.ItemImage;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Comparator;

@Getter
@Builder
public class AdminItemResDto {

    private Long itemId;
    private String title;
    private String sellerNickname;
    private Long price;
    private String tradeType;
    private String status;
    private String thumbnailImg;
    private boolean isDeletedByAdmin;
    private LocalDateTime createdAt;

    public static AdminItemResDto from(Item item) {
        String thumbnail = item.getImages().stream()
                .min(Comparator.comparingInt(ItemImage::getOrderNum))
                .map(ItemImage::getImageUrl)
                .orElse(null);

        return AdminItemResDto.builder()
                .itemId(item.getId())
                .title(item.getTitle())
                .sellerNickname(item.getSeller().getNickname())
                .price(item.getPrice())
                .tradeType(item.getTradeType().name())
                .status(item.getStatus().name())
                .thumbnailImg(thumbnail)
                .isDeletedByAdmin(item.isDeletedByAdmin())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
