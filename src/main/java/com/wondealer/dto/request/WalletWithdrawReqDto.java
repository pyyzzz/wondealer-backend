package com.wondealer.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class WalletWithdrawReqDto {

    @NotNull
    @Positive
    private Long amount;
}