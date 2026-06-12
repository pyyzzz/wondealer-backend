package com.wondealer.controller;

import com.wondealer.dto.request.ChangePasswordReqDto;
import com.wondealer.dto.request.UpdateBankReqDto;
import com.wondealer.dto.request.UpdateMemberInfoReqDto;
import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.MemberResDto;
import com.wondealer.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // GET /api/members/me — 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResDto>> getMyInfo() {
        // memberService.getMyInfo() 호출
        MemberResDto memberResDto = memberService.getMyInfo();
        return ResponseEntity.ok(ApiResponse.ok("내 정보 조회 성공", memberResDto));
    }


    // PATCH /api/members/me — 회원 정보 수정 (닉네임, 프로필 이미지-추후 예정???)
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberResDto>> updateMyInfo(@RequestBody UpdateMemberInfoReqDto dto) {
        // memberService.updateMyInfo(dto) 호출
        MemberResDto updatedMember = memberService.updateMyInfo(dto);

        // 2. 수정된 회원 정보 반환
        return ResponseEntity.ok(ApiResponse.ok("회원 정보가 수정되었습니다.", updatedMember));
    }

    // PATCH /api/members/me/password — 비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<?>> changePassword(@RequestBody ChangePasswordReqDto dto) {
        // 현재 비밀번호 검증 후 새 비밀번호로 변경
        memberService.changePassword(dto.getCurrentPassword(), dto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 변경되었습니다.", null));
    }

    // PATCH /api/members/me/bank — 계좌 등록/수정
    @PatchMapping("/me/bank")
    public ResponseEntity<ApiResponse<?>> updateBankInfo(@Valid @RequestBody UpdateBankReqDto dto) {
        memberService.updateBankInfo(dto);
        return ResponseEntity.ok(ApiResponse.ok("계좌 정보가 수정되었습니다."));
    }

    // GET /api/members/me/items — 내 판매 상품 목록
    @GetMapping("/me/items")
    public ResponseEntity<ApiResponse<?>> getMyItems(
            @RequestParam(defaultValue = "0") int page) {
        // TODO: 백엔드A 구현
        return null;
    }

    // GET /api/members/me/trades — 내 거래 내역
    @GetMapping("/me/trades")
    public ResponseEntity<ApiResponse<?>> getMyTrades(
            @RequestParam(defaultValue = "BUY") String type,
            @RequestParam(defaultValue = "0") int page) {
        // TODO: 백엔드A 구현
        return null;
    }

    // GET /api/members/me/bids — 내 입찰 내역
    @GetMapping("/me/bids")
    public ResponseEntity<ApiResponse<?>> getMyBids(
            @RequestParam(defaultValue = "0") int page) {
        // TODO: 백엔드A 구현
        return null;
    }
}
