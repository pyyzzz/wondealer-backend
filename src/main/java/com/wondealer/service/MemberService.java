package com.wondealer.service;

import com.wondealer.dto.request.UpdateBankReqDto;
import com.wondealer.dto.response.MemberResDto;
import com.wondealer.entity.Member;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.MemberRepository;
import com.wondealer.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    // ── 내 정보 조회 ──────────────────────────────────────────────
    public MemberResDto getMyInfo() {
        // TODO: 백엔드A 구현
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "내 정보 조회 미구현");
    }

    // ── 회원 정보 수정 ────────────────────────────────────────────
    @Transactional
    public MemberResDto updateMyInfo() {
        // TODO: 백엔드A 구현
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "회원 정보 수정 미구현");
    }

    // ── 비밀번호 변경 ─────────────────────────────────────────────
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        // TODO: 백엔드A 구현
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "비밀번호 변경 미구현");
    }

    // ── 계좌 등록/수정 ─────────────────────────────────────────────
    // Member 도메인 메서드 updateBankInfo() 사용
    @Transactional
    public void updateBankInfo(UpdateBankReqDto dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        member.updateBankInfo(dto.getBankName(), dto.getAccountNumber(), dto.getAccountHolder());
    }

    // ── 내 판매 상품 목록 조회 ─────────────────────────────────────
    public Object getMyItems(int page) {
        // TODO: 백엔드A 구현
        // ItemRepository 주입 후 findByMemberId() 조회
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "내 판매 상품 목록 미구현");
    }

    // ── 내 거래 내역 조회 ─────────────────────────────────────────
    public Object getMyTrades(String type, int page) {
        // TODO: 백엔드A 구현
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "거래 내역 조회 미구현");
    }

    // ── 내 입찰 내역 조회 ─────────────────────────────────────────
    public Object getMyBids(int page) {
        // TODO: 백엔드A 구현
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "입찰 내역 조회 미구현");
    }
}
