package com.wondealer.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberReqDto {

    private String nickName;        // 변경할 닉네임

    private String profileImg;      // 변경할 프로필 이미지

    private String bankName;        // 변경할 은행명

    private String accountNumber;   // 변경할 계좌번호

    private String accountHolder;   // 변경할 예금주
}
