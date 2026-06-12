package com.wondealer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class AuctionCreateReqDto {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Long categoryId;

    private Long serverId;

    @NotNull
    @Positive
    private Long startPrice;

    @Positive
    private Long instantBuyPrice;

    @NotNull
    private Integer auctionDays;
}
