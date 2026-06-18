package com.wondealer.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalletWithdrawResDto {

    private Long withdrawId;
    private Long amount;
    private String status;
}
