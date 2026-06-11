package com.wondealer.dto.response;

import com.wondealer.constant.Authority;
import com.wondealer.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// password 필드 없음 — 응답에서 비밀번호 절대 노출 금지
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResDto {

    private Long memberId;
    private String email;
    private String username;    // 아이디 (로그인 ID)
    private String name;        // 성함
    private String nickname;
    private String profileImg;
    private String bankName;        //은행이름
    private String accountNumber;   //계좌번호
    private String accountHolder;   //예금주
    private boolean isEmailVerified;
    private boolean isBanned;
    private Authority authority;

    // Entity → DTO 변환 정적 팩토리 메서드
    public static MemberResDto of(Member member) {
        return MemberResDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .username(member.getUsername())
                .name(member.getName())
                .nickname(member.getNickname())
                .profileImg(member.getProfileImg())
                .bankName(member.getBankName())
                .accountNumber(member.getAccountNumber())
                .accountHolder(member.getAccountHolder())
                .isEmailVerified(member.isEmailVerified())
                .isBanned(member.isBanned())
                .authority(member.getAuthority())
                .build();
    }
}
