package com.wondealer.controller;

import com.wondealer.dto.request.ChatRoomCreateReqDto;
import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.ChatMessageResDto;
import com.wondealer.dto.response.ChatReadResDto;
import com.wondealer.dto.response.ChatRoomCreateResDto;
import com.wondealer.dto.response.ChatRoomListResDto;
import com.wondealer.dto.response.PageResDto;
import com.wondealer.security.SecurityUtil;
import com.wondealer.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomCreateResDto>> createChatRoom(
            @Valid @RequestBody ChatRoomCreateReqDto dto
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        ChatRoomCreateResDto response = chatService.createChatRoom(memberId, dto);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResDto<ChatRoomListResDto>>> getMyChatRooms(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        PageResDto<ChatRoomListResDto> response = chatService.getMyChatRooms(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<PageResDto<ChatMessageResDto>>> getMessages(
            @PathVariable Long chatRoomId,
            @PageableDefault(size = 30) Pageable pageable
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        PageResDto<ChatMessageResDto> response = chatService.getMessages(memberId, chatRoomId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{chatRoomId}/read")
    public ResponseEntity<ApiResponse<ChatReadResDto>> markAsRead(
            @PathVariable Long chatRoomId
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        ChatReadResDto response = chatService.markAsRead(memberId, chatRoomId);
        return ResponseEntity.ok(ApiResponse.ok("읽음 처리되었습니다.", response));
    }
}