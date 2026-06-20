package com.wondealer.dto.response;

import com.wondealer.entity.Member;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminMemberResDto {

    private Long memberId;
    private String email;
    private String username;
    private String nickname;
    private boolean isBanned;
    private LocalDateTime createdAt;

    public static AdminMemberResDto from(Member member) {
        return AdminMemberResDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .isBanned(member.isBanned())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
