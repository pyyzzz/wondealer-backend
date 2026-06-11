package com.wondealer.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// 이메일 인증 발송용
public class EmailReqDto {
    private String email;
}
