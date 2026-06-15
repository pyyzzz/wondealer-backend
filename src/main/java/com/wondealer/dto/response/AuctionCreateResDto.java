package com.wondealer.dto.response;

import com.wondealer.entity.Auction;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AuctionCreateResDto {

    private Long auctionId;         // WebSocket 구독 시 필요 (/topic/auction/{auctionId})
    private Long itemId;
    private String title;
    private String tradeType;
    private String status;
    private Long startPrice;
    private Long currentPrice;      // 현재 입찰가 (= startPrice로 초기화)
    private Long instantBuyPrice;   // 즉시 낙찰가 (nullable)
    private LocalDateTime auctionEndTime;

    public static AuctionCreateResDto from(Auction auction) {
        return AuctionCreateResDto.builder()
                .auctionId(auction.getId())
                .itemId(auction.getItem().getId())
                .title(auction.getItem().getTitle())
                .tradeType(auction.getItem().getTradeType().name())
                .status(auction.getItem().getStatus().name())
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .instantBuyPrice(auction.getInstantBuyPrice())
                .auctionEndTime(auction.getEndTime())
                .build();
    }
}