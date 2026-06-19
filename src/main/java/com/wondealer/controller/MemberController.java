package com.wondealer.controller;

import com.wondealer.dto.request.ChangePasswordReqDto;
import com.wondealer.dto.request.UpdateBankReqDto;
import com.wondealer.dto.request.UpdateMemberInfoReqDto;
import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.ItemListResDto;
import com.wondealer.dto.response.MemberResDto;
import com.wondealer.dto.response.MyBidResDto;
import com.wondealer.dto.response.MyTradeResDto;
import com.wondealer.dto.response.PageResDto;
import com.wondealer.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResDto>> getMyInfo() {
        MemberResDto response = memberService.getMyInfo();
        return ResponseEntity.ok(ApiResponse.ok("내 정보 조회 성공", response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberResDto>> updateMyInfo(
            @RequestBody UpdateMemberInfoReqDto dto) {
        MemberResDto response = memberService.updateMyInfo(dto);
        return ResponseEntity.ok(ApiResponse.ok("회원 정보가 수정되었습니다.", response));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @RequestBody ChangePasswordReqDto dto) {
        memberService.changePassword(dto.getCurrentPassword(), dto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 변경되었습니다.", null));
    }

    @PatchMapping("/me/bank")
    public ResponseEntity<ApiResponse<?>> updateBankInfo(
            @Valid @RequestBody UpdateBankReqDto dto) {
        memberService.updateBankInfo(dto);
        return ResponseEntity.ok(ApiResponse.ok("계좌 정보가 수정되었습니다."));
    }

    @GetMapping("/me/items")
    public ResponseEntity<ApiResponse<PageResDto<ItemListResDto>>> getMyItems(
            @RequestParam(defaultValue = "0") int page) {
        Page<ItemListResDto> response = memberService.getMyItems(page);
        return ResponseEntity.ok(ApiResponse.ok("내 판매 상품 목록 조회 성공",
                PageResDto.from(response)));
    }

    @GetMapping("/me/trades")
    public ResponseEntity<ApiResponse<PageResDto<MyTradeResDto>>> getMyTrades(
            @RequestParam(defaultValue = "BUY") String type,
            @RequestParam(defaultValue = "0") int page) {
        Page<MyTradeResDto> response = memberService.getMyTrades(type, page);
        return ResponseEntity.ok(ApiResponse.ok("내 거래 내역 조회 성공",
                PageResDto.from(response)));
    }

    @GetMapping("/me/bids")
    public ResponseEntity<ApiResponse<PageResDto<MyBidResDto>>> getMyBids(
            @RequestParam(defaultValue = "0") int page) {
        Page<MyBidResDto> response = memberService.getMyBids(page);
        return ResponseEntity.ok(ApiResponse.ok("내 입찰 내역 조회 성공",
                PageResDto.from(response)));
    }
}
