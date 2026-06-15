package com.wondealer.dto.response;

import com.wondealer.entity.Auction;
import com.wondealer.entity.Item;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AuctionListResDto {

    private Long auctionId;
    private Long itemId;
    private String itemTitle;
    private String gameName;        // 어떤 게임 경매인지
    private String thumbnailImg;
    private Long startPrice;
    private Long currentPrice;
    private Long instantBuyPrice;   // 즉시 낙찰가 (nullable)
    private Integer bidCount;
    private LocalDateTime endTime;
    private String status;

    public static AuctionListResDto from(Auction auction) {
        Item item = auction.getItem();
        String thumbnailImg = item.getImages().isEmpty()
                ? null
                : item.getImages().get(0).getImageUrl();

        return AuctionListResDto.builder()
                .auctionId(auction.getId())
                .itemId(item.getId())
                .itemTitle(item.getTitle())
                .gameName(item.getGameCategory().getGame().getGameName())
                .thumbnailImg(thumbnailImg)
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .instantBuyPrice(auction.getInstantBuyPrice())
                .bidCount(auction.getBidCount())
                .endTime(auction.getEndTime())
                .status(auction.getStatus())
                .build();
    }
}