package com.wondealer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateBankReqDto {

    @NotBlank(message = "은행명을 입력해주세요.")
    private String bankName;        // 은행명 (예: 신한은행)

    @NotBlank(message = "계좌번호를 입력해주세요.")
    private String accountNumber;   // 계좌번호

    @NotBlank(message = "예금주명을 입력해주세요.")
    private String accountHolder;   // 예금주명
}
