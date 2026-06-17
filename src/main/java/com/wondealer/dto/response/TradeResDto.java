package com.wondealer.dto.response;

import com.wondealer.entity.Trade;
import com.wondealer.entity.TradeStatus;
import com.wondealer.entity.TradeType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradeResDto {

    private Long tradeId;
    private Long itemId;
    private Long sellerId;
    private Long buyerId;
    private Long tradePrice;
    private TradeType tradeType;
    private TradeStatus status;

    public static TradeResDto from(Trade trade) {
        return TradeResDto.builder()
                .tradeId(trade.getId())
                .itemId(trade.getItem().getId())
                .sellerId(trade.getSeller().getId())
                .buyerId(trade.getBuyer().getId())
                .tradePrice(trade.getTradePrice())
                .tradeType(trade.getTradeType())
                .status(trade.getStatus())
                .build();
    }
}