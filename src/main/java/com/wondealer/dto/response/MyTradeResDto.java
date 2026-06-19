package com.wondealer.dto.response;

import com.wondealer.entity.Trade;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 마이페이지 거래 내역 전용 DTO
 * 기존 TradeResDto는 거래/결제 API에서 사용 중이므로 분리
 */
@Getter
@Builder
public class MyTradeResDto {

    private Long tradeId;
    private String itemTitle;
    private Long tradePrice;
    private String tradeType;
    private String status;
    private LocalDateTime createdAt;

    public static MyTradeResDto from(Trade trade) {
        return MyTradeResDto.builder()
                .tradeId(trade.getId())
                .itemTitle(trade.getItem().getTitle())
                .tradePrice(trade.getTradePrice())
                .tradeType(trade.getTradeType().name())
                .status(trade.getStatus().name())
                .createdAt(trade.getCreatedAt())
                .build();
    }
}
