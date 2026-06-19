package com.wondealer.dto.response;

import com.wondealer.entity.ChatRoom;
import com.wondealer.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomCreateResDto {

    private Long chatRoomId;
    private String roomType;
    private String itemTitle;
    private OpponentDto opponent;
    private boolean isNew;
    private LocalDateTime expiresAt;

    public static ChatRoomCreateResDto from(ChatRoom chatRoom, Member opponent, boolean isNew) {
        return ChatRoomCreateResDto.builder()
                .chatRoomId(chatRoom.getId())
                .roomType(chatRoom.getTrade() == null ? "INQUIRY" : "TRADE")
                .itemTitle(chatRoom.getItem().getTitle())
                .opponent(OpponentDto.from(opponent))
                .isNew(isNew)
                .expiresAt(chatRoom.getExpiresAt())
                .build();
    }

    @Getter
    @Builder
    public static class OpponentDto {
        private Long memberId;
        private String nickname;
        private String profileImg;

        public static OpponentDto from(Member member) {
            return OpponentDto.builder()
                    .memberId(member.getId())
                    .nickname(member.getNickname())
                    .profileImg(member.getProfileImg())
                    .build();
        }
    }
}