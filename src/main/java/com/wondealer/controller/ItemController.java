package com.wondealer.controller;

import com.wondealer.dto.request.ItemReqDto;
import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.ItemDetailResDto;
import com.wondealer.dto.response.ItemResDto;
import com.wondealer.security.SecurityUtil;
import com.wondealer.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<ItemResDto>> createDirectItem(
            @Valid @RequestBody ItemReqDto dto
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        ItemResDto response = itemService.createDirectItem(memberId, dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("판매 상품이 등록되었습니다.", response));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemDetailResDto>> getItemDetail(
            @PathVariable Long itemId
    ) {
        ItemDetailResDto response = itemService.getItemDetail(itemId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}