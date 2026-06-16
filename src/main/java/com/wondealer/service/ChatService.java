package com.wondealer.service;

import com.wondealer.dto.websocket.ChatBroadcastDto;
import com.wondealer.dto.websocket.ChatRequestDto;
import com.wondealer.entity.ChatMessage;
import com.wondealer.entity.ChatRoom;
import com.wondealer.entity.Member;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.ChatMessageRepository;
import com.wondealer.repository.ChatRoomRepository;
import com.wondealer.repository.MemberRepository;
import com.wondealer.security.TokenProvider;
import lombok.RequiredArgsConstructor;
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

    /**
     * 채팅 메시지를 처리한다.
     * 토큰 검증 → 채팅방 활성화 확인 → DB 저장 → 브로드캐스트 DTO 반환
     */
    @Transactional
    public ChatBroadcastDto handleMessage(String token, Long chatRoomId, ChatRequestDto dto) {

        // 1. 토큰 유효성 검사 (만료 토큰 차단)
        String pureToken = token.replace("Bearer ", "");
        if (!tokenProvider.validateToken(pureToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        // 2. memberId 추출
        Long senderId = tokenProvider.getMemberIdFromToken(pureToken);

        // 3. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        // 4. isActive 체크 — 비활성화된 채팅방은 전송 불가 (우리 설계 확정)
        if (!chatRoom.isActive()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "종료된 채팅방입니다. 메시지를 보낼 수 없습니다.");
        }

        // 5. 발신자 조회
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        // 6. 메시지 DB 저장
        ChatMessage saved = chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .sender(sender)
                        .content(dto.getContent())
                        .build()
        );

        // 7. 브로드캐스트 DTO 반환
        return ChatBroadcastDto.builder()
                .chatRoomId(chatRoomId)
                .senderId(sender.getId())
                .senderNickname(sender.getNickname())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
