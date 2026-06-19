package com.wondealer.dto.response;

import com.wondealer.entity.Bid;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 마이페이지 입찰 내역 전용 DTO
 */
@Getter
@Builder
public class MyBidResDto {

    private Long bidId;
    private Long auctionId;
    private String itemTitle;
    private Long bidPrice;
    private String status;
    private LocalDateTime bidTime;

    public static MyBidResDto from(Bid bid) {
        return MyBidResDto.builder()
                .bidId(bid.getId())
                .auctionId(bid.getAuction().getId())
                .itemTitle(bid.getAuction().getItem().getTitle())
                .bidPrice(bid.getBidPrice())
                .status(bid.getStatus())
                .bidTime(bid.getBidTime())
                .build();
    }
}
