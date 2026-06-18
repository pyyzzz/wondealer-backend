package com.wondealer.controller;

import com.wondealer.dto.request.WalletChargeCompleteReqDto;
import com.wondealer.dto.request.WalletChargeReadyReqDto;
import com.wondealer.dto.request.WalletWithdrawReqDto;
import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.WalletChargeCompleteResDto;
import com.wondealer.dto.response.WalletChargeReadyResDto;
import com.wondealer.dto.response.WalletWithdrawResDto;
import com.wondealer.security.SecurityUtil;
import com.wondealer.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/charges/ready")
    public ResponseEntity<ApiResponse<WalletChargeReadyResDto>> prepareCharge(
            @Valid @RequestBody WalletChargeReadyReqDto dto
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        WalletChargeReadyResDto response = walletService.prepareCharge(memberId, dto);
        return ResponseEntity.ok(ApiResponse.ok("WonPay 충전 준비가 완료되었습니다.", response));
    }

    @PostMapping("/charges/complete")
    public ResponseEntity<ApiResponse<WalletChargeCompleteResDto>> completeCharge(
            @Valid @RequestBody WalletChargeCompleteReqDto dto
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        WalletChargeCompleteResDto response = walletService.completeCharge(memberId, dto);
        return ResponseEntity.ok(ApiResponse.ok("WonPay 충전이 완료되었습니다.", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WalletWithdrawResDto>> withdraw(
            @Valid @RequestBody WalletWithdrawReqDto dto
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        WalletWithdrawResDto response = walletService.withdraw(memberId, dto);
        return ResponseEntity.ok(ApiResponse.ok("출금 신청이 완료되었습니다. 영업일 기준 1~3일 내 처리됩니다.", response));
    }
}
