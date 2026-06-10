package com.wondealer.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// 이메일 인증 확인용
public class EmailVerifyReqDto {
    private String token;
}
