package com.wondealer.dto.response;

import com.wondealer.entity.ChatMessage;
import com.wondealer.entity.ChatRoom;
import com.wondealer.entity.ItemImage;
import com.wondealer.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;

@Getter
@Builder
public class ChatRoomListResDto {

    private Long chatRoomId;
    private Long tradeId;
    private String tradeStatus;
    private Long itemId;
    private Long itemPrice;
    private String itemTitle;
    private String thumbnailImg;
    private ChatRoomCreateResDto.OpponentDto opponent;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
    private boolean isActive;
    private LocalDateTime expiresAt;

    public static ChatRoomListResDto from(ChatRoom chatRoom, Long memberId, ChatMessage lastMessage, long unreadCount) {
        Member opponent = chatRoom.getBuyer().getId().equals(memberId)
                ? chatRoom.getSeller()
                : chatRoom.getBuyer();

        return ChatRoomListResDto.builder()
                .chatRoomId(chatRoom.getId())
                .tradeId(chatRoom.getTrade() == null ? null : chatRoom.getTrade().getId())
                .tradeStatus(chatRoom.getTrade() == null ? null : chatRoom.getTrade().getStatus().name())
                .itemId(chatRoom.getItem().getId())
                .itemPrice(chatRoom.getItem().getPrice())
                .itemTitle(chatRoom.getItem().getTitle())
                .thumbnailImg(findThumbnail(chatRoom))
                .opponent(ChatRoomCreateResDto.OpponentDto.from(opponent))
                .lastMessage(lastMessage == null ? null : lastMessage.getContent())
                .lastMessageAt(lastMessage == null ? null : lastMessage.getCreatedAt())
                .unreadCount(unreadCount)
                .isActive(chatRoom.isActive())
                .expiresAt(chatRoom.getExpiresAt())
                .build();
    }

    private static String findThumbnail(ChatRoom chatRoom) {
        return chatRoom.getItem().getImages().stream()
                .min(Comparator.comparingInt(ItemImage::getOrderNum))
                .map(ItemImage::getImageUrl)
                .orElse(null);
    }
}