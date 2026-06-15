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
    private String thumbnailImg;
    private Long startPrice;
    private Long currentPrice;
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
                .thumbnailImg(thumbnailImg)
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .bidCount(auction.getBidCount())
                .endTime(auction.getEndTime())
                .status(auction.getStatus())
                .build();
    }
}
