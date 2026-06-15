package com.wondealer.controller;

import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.AuctionDetailResDto;
import com.wondealer.dto.response.AuctionListResDto;
import com.wondealer.dto.response.PageResDto;
import com.wondealer.security.SecurityUtil;
import com.wondealer.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResDto<AuctionListResDto>>> getAuctions(
            @RequestParam(required = false) Long gameId,
            @PageableDefault(size = 10, sort = "endTime") Pageable pageable
    ) {
        PageResDto<AuctionListResDto> response = auctionService.getAuctions(gameId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionDetailResDto>> getAuctionDetail(
            @PathVariable Long auctionId
    ) {
        AuctionDetailResDto response = auctionService.getAuctionDetail(auctionId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<Void>> cancelAuction(
            @PathVariable Long auctionId
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        auctionService.cancelAuction(memberId, auctionId);
        return ResponseEntity.ok(ApiResponse.ok("경매가 취소되었습니다.", null));
    }
}
