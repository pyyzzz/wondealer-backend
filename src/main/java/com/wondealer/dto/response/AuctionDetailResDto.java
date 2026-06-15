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
        private List<String> images;
        private SellerInfo seller;

        private static ItemInfo from(Item item) {
            return ItemInfo.builder()
                    .itemId(item.getId())
                    .title(item.getTitle())
                    .description(item.getDescription())
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
