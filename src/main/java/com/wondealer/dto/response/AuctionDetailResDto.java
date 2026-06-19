package com.wondealer.dto.response;

import com.wondealer.entity.Auction;
import com.wondealer.entity.Item;
import com.wondealer.entity.Member;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AuctionDetailResDto {

    private Long auctionId;
    private ItemInfo item;
    private Long startPrice;
    private Long currentPrice;
    private Long instantBuyPrice;   // 즉시 낙찰가 (nullable)
    private Integer bidCount;
    private LocalDateTime endTime;
    private String status;
    private Long winnerId;

    public static AuctionDetailResDto from(Auction auction) {
        return AuctionDetailResDto.builder()
                .auctionId(auction.getId())
                .item(ItemInfo.from(auction.getItem()))
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .instantBuyPrice(auction.getInstantBuyPrice())
                .bidCount(auction.getBidCount())
                .endTime(auction.getEndTime())
                .status(auction.getStatus())
                .winnerId(auction.getWinner() == null ? null : auction.getWinner().getId())
                .build();
    }

    @Getter
    @Builder
    public static class ItemInfo {
        private Long itemId;
        private String title;
        private String description;
        private String gameName;        // 게임명
        private String categoryName;    // 카테고리 (아이템/게임머니/계정/기타)
        private String serverName;      // 서버명 (nullable, 서버 없는 게임은 null)
        private List<String> images;
        private SellerInfo seller;

        private static ItemInfo from(Item item) {
            return ItemInfo.builder()
                    .itemId(item.getId())
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .gameName(item.getGameCategory().getGame().getGameName())
                    .categoryName(item.getGameCategory().getCategoryName())
                    .serverName(item.getGameServer() == null
                            ? null
                            : item.getGameServer().getServerName())
                    .images(item.getImages().stream()
                            .map(image -> image.getImageUrl())
                            .toList())
                    .seller(SellerInfo.from(item.getSeller()))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class SellerInfo {
        private Long memberId;
        private String nickname;

        private static SellerInfo from(Member seller) {
            return SellerInfo.builder()
                    .memberId(seller.getId())
                    .nickname(seller.getNickname())
                    .build();
        }
    }
}