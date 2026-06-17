package com.wondealer.service;

import com.wondealer.dto.request.ChatRoomCreateReqDto;
import com.wondealer.dto.response.ChatMessageResDto;
import com.wondealer.dto.response.ChatReadResDto;
import com.wondealer.dto.response.ChatRoomCreateResDto;
import com.wondealer.dto.response.ChatRoomListResDto;
import com.wondealer.dto.response.PageResDto;
import com.wondealer.dto.websocket.ChatBroadcastDto;
import com.wondealer.dto.websocket.ChatRequestDto;
import com.wondealer.entity.ChatMessage;
import com.wondealer.entity.ChatRoom;
import com.wondealer.entity.Item;
import com.wondealer.entity.ItemStatus;
import com.wondealer.entity.Member;
import com.wondealer.entity.TradeType;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.ChatMessageRepository;
import com.wondealer.repository.ChatRoomRepository;
import com.wondealer.repository.ItemRepository;
import com.wondealer.repository.MemberRepository;
import com.wondealer.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final TokenProvider tokenProvider;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public ChatRoomCreateResDto createChatRoom(Long buyerId, ChatRoomCreateReqDto dto) {
        Member buyer = findMember(buyerId);
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        validateInquiryItem(buyerId, item);

        return chatRoomRepository.findByItemIdAndBuyerId(item.getId(), buyerId)
                .map(chatRoom -> ChatRoomCreateResDto.from(chatRoom, chatRoom.getSeller(), false))
                .orElseGet(() -> {
                    ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder()
                            .buyer(buyer)
                            .seller(item.getSeller())
                            .item(item)
                            .build());
                    return ChatRoomCreateResDto.from(chatRoom, item.getSeller(), true);
                });
    }

    public PageResDto<ChatRoomListResDto> getMyChatRooms(Long memberId, Pageable pageable) {
        Page<ChatRoomListResDto> rooms = chatRoomRepository.findByBuyerIdOrSellerId(memberId, memberId, pageable)
                .map(chatRoom -> {
                    ChatMessage lastMessage = chatMessageRepository
                            .findTopByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId())
                            .orElse(null);
                    long unreadCount = chatMessageRepository
                            .countByChatRoomIdAndIsReadFalseAndSenderIdNot(chatRoom.getId(), memberId);
                    return ChatRoomListResDto.from(chatRoom, memberId, lastMessage, unreadCount);
                });

        return PageResDto.from(rooms);
    }

    public PageResDto<ChatMessageResDto> getMessages(Long memberId, Long chatRoomId, Pageable pageable) {
        ChatRoom chatRoom = findChatRoomForParticipant(memberId, chatRoomId);
        Page<ChatMessageResDto> messages = chatMessageRepository
                .findByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId(), pageable)
                .map(ChatMessageResDto::from);

        return PageResDto.from(messages);
    }

    @Transactional
    public ChatReadResDto markAsRead(Long memberId, Long chatRoomId) {
        ChatRoom chatRoom = findChatRoomForParticipant(memberId, chatRoomId);
        int updatedCount = chatMessageRepository.markAllAsRead(chatRoom.getId(), memberId);
        return ChatReadResDto.of(updatedCount);
    }

    /**
     * WebSocket 메시지 전송 처리.
     * ChatWebSocketController는 유지하고, 기존 메시지 저장/브로드캐스트 흐름을 그대로 사용한다.
     */
    @Transactional
    public ChatBroadcastDto handleMessage(String token, Long chatRoomId, ChatRequestDto dto) {
        String pureToken = token.replace("Bearer ", "");
        if (!tokenProvider.validateToken(pureToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        Long senderId = tokenProvider.getMemberIdFromToken(pureToken);
        ChatRoom chatRoom = findChatRoomForParticipant(senderId, chatRoomId);

        if (!chatRoom.isActive()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "종료된 채팅방입니다. 메시지를 보낼 수 없습니다.");
        }

        Member sender = findMember(senderId);
        ChatMessage saved = chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(sender)
                        .content(dto.getContent())
                        .build()
        );

        return ChatBroadcastDto.builder()
                .chatRoomId(chatRoomId)
                .senderId(sender.getId())
                .senderNickname(sender.getNickname())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private void validateInquiryItem(Long buyerId, Item item) {
        if (item.getTradeType() != TradeType.DIRECT) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "DIRECT 상품만 문의 채팅방을 개설할 수 있습니다.");
        }

        if (item.getStatus() != ItemStatus.SELLING || item.isDeletedByAdmin()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "판매 중인 상품만 문의 채팅방을 개설할 수 있습니다.");
        }

        if (item.getSeller().getId().equals(buyerId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "본인 상품에는 채팅방을 개설할 수 없습니다.");
        }
    }

    private ChatRoom findChatRoomForParticipant(Long memberId, Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        if (!isParticipant(chatRoom, memberId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "채팅방 참여자만 접근할 수 있습니다.");
        }

        return chatRoom;
    }

    private boolean isParticipant(ChatRoom chatRoom, Long memberId) {
        return chatRoom.getBuyer().getId().equals(memberId)
                || chatRoom.getSeller().getId().equals(memberId);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}