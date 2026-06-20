package com.wondealer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminBanReqDto {

    @NotBlank(message = "제재 사유를 입력해주세요.")
    private String reason;
}
