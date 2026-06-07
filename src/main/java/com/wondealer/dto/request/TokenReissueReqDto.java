package com.wondealer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenReissueReqDto {

    private String accessToken;   // 만료된 Access Token (memberId 추출용)
    private String refreshToken;  // DB 저장된 Refresh Token과 일치 여부 검증용
}
