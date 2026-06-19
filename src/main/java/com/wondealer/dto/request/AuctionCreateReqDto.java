package com.wondealer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import java.util.List;

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

    @Size(max = 5, message = "이미지는 최대 5장까지 등록 가능합니다.")
    private List<String> imageUrls;
}