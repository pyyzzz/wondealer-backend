package com.wondealer.controller;

import com.wondealer.dto.request.TradeCreateReqDto;
import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.TradeResDto;
import com.wondealer.security.SecurityUtil;
import com.wondealer.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public ResponseEntity<ApiResponse<TradeResDto>> createDirectTrade(
            @Valid @RequestBody TradeCreateReqDto dto
    ) {
        Long buyerId = SecurityUtil.getCurrentMemberId();
        TradeResDto response = tradeService.createDirectTrade(buyerId, dto);
        return ResponseEntity.ok(ApiResponse.ok("거래가 생성되었습니다.", response));
    }

    @PostMapping("/{tradeId}/confirm")
    public ResponseEntity<ApiResponse<TradeResDto>> confirmTrade(
            @PathVariable Long tradeId
    ) {
        Long buyerId = SecurityUtil.getCurrentMemberId();
        TradeResDto response = tradeService.confirmTrade(buyerId, tradeId);
        return ResponseEntity.ok(ApiResponse.ok("거래가 확정되었습니다.", response));
    }
}