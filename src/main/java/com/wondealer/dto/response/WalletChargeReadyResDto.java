package com.wondealer.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalletChargeReadyResDto {

    private String paymentId;
    private Long amount;
    private String orderName;
    private String currency;
}
