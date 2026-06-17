package com.wondealer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TradeCreateReqDto {

    @NotNull
    private Long itemId;

    @NotBlank
    private String paymentMethod;

    private String paymentId;
}