package com.wondealer.controller;

import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.AuctionDetailResDto;
import com.wondealer.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @GetMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionDetailResDto>> getAuctionDetail(
            @PathVariable Long auctionId
    ) {
        AuctionDetailResDto response = auctionService.getAuctionDetail(auctionId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
