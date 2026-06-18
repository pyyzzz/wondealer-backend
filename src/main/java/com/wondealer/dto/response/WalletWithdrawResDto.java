package com.wondealer.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalletWithdrawResDto {

    private Long withdrawId;
    private Long amount;       // 출금 신청 금액
    private Long fee;          // 수수료 (1%)
    private Long actualAmount; // 실제 입금 금액 (수수료 차감 후)
    private String bankName;
    private String accountNumber; // 마스킹 처리됨
    private String status;
}