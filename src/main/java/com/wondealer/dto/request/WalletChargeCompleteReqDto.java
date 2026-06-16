package com.wondealer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class WalletChargeCompleteReqDto {

    @NotBlank
    private String paymentId;
}
