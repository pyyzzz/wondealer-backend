package com.wondealer.controller;

import com.wondealer.dto.request.AuctionCreateReqDto;
import com.wondealer.dto.request.ItemCreateReqDto;
import com.wondealer.dto.request.ItemUpdateReqDto;
import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.AuctionCreateResDto;
import com.wondealer.dto.response.ItemCreateResDto;
import com.wondealer.dto.response.ItemDetailResDto;
import com.wondealer.dto.response.ItemListResDto;
import com.wondealer.dto.response.PageResDto;
import com.wondealer.security.SecurityUtil;
import com.wondealer.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResDto<ItemListResDto>>> getItems(
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) Long serverId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tradeType,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<ItemListResDto> response = itemService.getItems(gameId, serverId, categoryId, tradeType, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResDto.from(response)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItemCreateResDto>> createDirectItem(
            @Valid @RequestBody ItemCreateReqDto dto
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        ItemCreateResDto response = itemService.createDirectItem(memberId, dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("판매 상품이 등록되었습니다.", response));
    }

    @PostMapping("/auction")
    public ResponseEntity<ApiResponse<AuctionCreateResDto>> createAuctionItem(
            @Valid @RequestBody AuctionCreateReqDto dto
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        AuctionCreateResDto response = itemService.createAuctionItem(memberId, dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("경매 상품이 등록되었습니다.", response));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemDetailResDto>> getItemDetail(
            @PathVariable Long itemId
    ) {
        ItemDetailResDto response = itemService.getItemDetail(itemId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemCreateResDto>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemUpdateReqDto dto
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        ItemCreateResDto response = itemService.updateItem(memberId, itemId, dto);
        return ResponseEntity.ok(ApiResponse.ok("상품이 수정되었습니다.", response));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long itemId
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        itemService.deleteItem(memberId, itemId);
        return ResponseEntity.ok(ApiResponse.ok("상품이 삭제되었습니다.", null));
    }
}
