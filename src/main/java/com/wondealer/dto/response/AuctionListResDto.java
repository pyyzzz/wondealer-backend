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
    private String gameName;
    private String categoryName;    // 아이템/게임머니/계정/기타
    private String serverName;      // nullable (서버 없는 게임은 null)
    private String thumbnailImg;
    private Long startPrice;
    private Long currentPrice;
    private Long instantBuyPrice;
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
                .categoryName(item.getGameCategory().getCategoryName())
                .serverName(item.getGameServer() == null
                        ? null
                        : item.getGameServer().getServerName())
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