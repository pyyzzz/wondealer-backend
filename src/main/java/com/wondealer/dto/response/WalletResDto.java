package com.wondealer.dto.response;

import com.wondealer.entity.Member;
import com.wondealer.entity.Wallet;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalletResDto {

    private Long balance;
    private String bankName;        // nullable (계좌 미등록 시 null)
    private String accountNumber;   // 마스킹 처리됨 (nullable)

    public static WalletResDto of(Wallet wallet, Member member) {
        String maskedAccount = null;
        if (member.getAccountNumber() != null) {
            String acc = member.getAccountNumber();
            maskedAccount = acc.length() > 4
                    ? "****" + acc.substring(acc.length() - 4)
                    : acc;
        }

        return WalletResDto.builder()
                .balance(wallet.getBalance())
                .bankName(member.getBankName())
                .accountNumber(maskedAccount)
                .build();
    }
}
