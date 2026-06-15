package com.wondealer.dto.response;

import com.wondealer.entity.Auction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuctionCreateResDto {

    private Long itemId;
    private String title;
    private String tradeType;
    private String status;
    private Long startPrice;
    private LocalDateTime auctionEndTime;

    public static AuctionCreateResDto from(Auction auction) {
        return AuctionCreateResDto.builder()
                .itemId(auction.getItem().getId())
                .title(auction.getItem().getTitle())
                .tradeType(auction.getItem().getTradeType().name())
                .status(auction.getItem().getStatus().name())
                .startPrice(auction.getStartPrice())
                .auctionEndTime(auction.getEndTime())
                .build();
    }
}
