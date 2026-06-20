package com.wondealer.dto.response;

import com.wondealer.constant.Authority;
import com.wondealer.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResDto {

    private Long memberId;
    private String email;
    private String username;
    private String name;
    private String nickname;
    private String profileImg;
    private boolean isEmailVerified;
    private boolean isBanned;
    private Authority authority;
    private String phone;

    // ── WonPay 출금 계좌 정보 ─────────────────────────────────────
    private String bankName;
    private String accountNumber;
    private String accountHolder;

    public static MemberResDto of(Member member) {
        return MemberResDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .username(member.getUsername())
                .name(member.getName())
                .nickname(member.getNickname())
                .profileImg(member.getProfileImg())
                .isEmailVerified(member.isEmailVerified())
                .isBanned(member.isBanned())
                .authority(member.getAuthority())
                .phone(member.getPhone())
                .bankName(member.getBankName())
                .accountNumber(member.getAccountNumber())
                .accountHolder(member.getAccountHolder())
                .build();
    }
}