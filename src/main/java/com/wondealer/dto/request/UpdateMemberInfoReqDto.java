package com.wondealer.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberInfoReqDto {
    private String nickname;
    private String phone;
    private String profileImg; // Firebase 이미지 URL
}