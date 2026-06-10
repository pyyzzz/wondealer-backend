package com.wondealer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 사용자 이름, 인증용 이메일
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FindUsernameReqDto {
    private String name;
    private String email;
}
