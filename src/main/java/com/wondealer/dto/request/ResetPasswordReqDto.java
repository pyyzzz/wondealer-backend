package com.wondealer.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordReqDto {
    // 비밀번호 재설정을 요청할 사용자의 이메일
    private String email;
}
