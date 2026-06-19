package com.wondealer.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class AuctionBidReqDto {

    @NotNull
    @Positive
    private Long bidPrice;
}