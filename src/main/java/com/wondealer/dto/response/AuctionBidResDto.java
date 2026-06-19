package com.wondealer.dto.response;

import com.wondealer.entity.Auction;
import com.wondealer.entity.Bid;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuctionBidResDto {

    private Long auctionId;
    private Long bidId;
    private Long currentPrice;
    private Integer bidCount;
    private String auctionStatus;
    private Long bidderId;
    private String bidderNickname;

    public static AuctionBidResDto from(Auction auction, Bid bid) {
        return AuctionBidResDto.builder()
                .auctionId(auction.getId())
                .bidId(bid.getId())
                .currentPrice(auction.getCurrentPrice())
                .bidCount(auction.getBidCount())
                .auctionStatus(auction.getStatus())
                .bidderId(bid.getBidder().getId())
                .bidderNickname(bid.getBidder().getNickname())
                .build();
    }
}