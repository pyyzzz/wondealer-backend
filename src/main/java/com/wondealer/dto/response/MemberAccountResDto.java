package com.wondealer.dto.response;

import com.wondealer.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberAccountResDto {
    private String nickname;        // 닉네임
    private String profileImg;      // 프로필 이미지
    private String bankName;        // 은행이름
    private String accountNumber;   // 계좌번호
    private String accountHolder;   // 예금주

    public static MemberAccountResDto of(Member member, String maskedAccountNumber) {
        return MemberAccountResDto.builder()
                .nickname(member.getNickname())
                .profileImg(member.getProfileImg())
                .bankName(member.getBankName())
                .accountNumber(maskedAccountNumber)
                .accountHolder(member.getAccountHolder())
                .build();
    }
}
