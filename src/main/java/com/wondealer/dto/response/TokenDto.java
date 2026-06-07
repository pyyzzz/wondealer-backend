package com.wondealer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TokenDto {

    private String grantType;           // 토큰 타입 — 항상 "Bearer"
    private String accessToken;         // 1시간 만료
    private String refreshToken;        // 7일 만료 — DB 저장
    private Long accessTokenExpiresIn;  // Access Token 만료 시각 (Unix timestamp ms)
    private String nickname;            // 로그인한 회원 닉네임 (프론트 헤더 표시용)
}
