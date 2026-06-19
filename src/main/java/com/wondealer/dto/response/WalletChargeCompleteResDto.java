package com.wondealer.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalletChargeCompleteResDto {

    private Long balance;
    private Long chargedAmount;
}
