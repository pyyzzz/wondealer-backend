package com.wondealer.dto.websocket;

import lombok.Builder;
import lombok.Getter;

/**
 * 경매 입찰 브로드캐스트 DTO
 * BidService에서 SimpMessagingTemplate으로 전송 시 사용:
 *   messagingTemplate.convertAndSend("/topic/auction/" + auctionId, dto)
 */
@Getter
@Builder
public class AuctionBidBroadcastDto {

    private Long auctionId;
    private Long currentPrice;
    private Integer bidCount;
    private Long bidderId;
    private String bidderNickname;
}