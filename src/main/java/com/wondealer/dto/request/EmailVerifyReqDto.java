package com.wondealer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// 이메일 인증 확인용
public class EmailVerifyReqDto {
    @NotBlank(message = "인증 토큰이 없습니다.")
    private String token;
}
