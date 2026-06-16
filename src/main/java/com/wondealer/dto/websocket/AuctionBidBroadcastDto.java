package com.wondealer.dto.websocket;

import lombok.Builder;
import lombok.Getter;

/**
 * 경매 입찰 브로드캐스트 DTO
 * 변후민 BidService에서 SimpMessagingTemplate으로 전송 시 사용:
 *   messagingTemplate.convertAndSend("/topic/auction/" + auctionId, dto)
 */
@Getter
@Builder
public class AuctionBidBroadcastDto {

    private Long auctionId;
    private Long currentPrice;   // 현재 최고 입찰가
    private Integer bidCount;    // 총 입찰 횟수
    private Long bidderId;       // 입찰자 ID
    private String bidderNickname; // 입찰자 닉네임
}
