package com.wondealer.service;

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
    @Transactional(readOnly = true)  // 조회 전용 트랜잭션
    public MemberResDto getMyInfo() {
        // SecurityUtil.getCurrentMemberId()로 현재 로그인 회원 ID 조회
        Long memberId = SecurityUtil.getCurrentMemberId();

        // memberRepository.findById() → MemberResDto.of() 반환 (없으면 예외 발생)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원정보를 찾을 수 없습니다"));
        return MemberResDto.of(member);
    }

    // ── 회원 정보 수정 ────────────────────────────────────────────
    @Transactional
    public MemberResDto updateMyInfo(/* TODO: UpdateMemberReqDto dto */) {
        // TODO: 백엔드A 구현
        // 닉네임, 프로필 이미지, 계좌 정보 수정
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "회원 정보 수정 미구현");
    }

    // ── 비밀번호 변경 ─────────────────────────────────────────────
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        // TODO: 백엔드A 구현
        // 1. 현재 비밀번호 BCrypt 검증
        // 2. 새 비밀번호 암호화 후 UPDATE
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "비밀번호 변경 미구현");
    }

    // ── 내 거래 내역 조회 ─────────────────────────────────────────
    public Object getMyTrades(String type, /* TODO: Pageable pageable */ int page) {
        // TODO: 백엔드A 구현
        // type = BUY / SELL 로 구매/판매 내역 구분
        // TRADE 테이블에서 buyer_id 또는 seller_id = 나 조건으로 조회
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "거래 내역 조회 미구현");
    }

    // ── 내 입찰 내역 조회 ─────────────────────────────────────────
    public Object getMyBids(/* TODO: Pageable pageable */ int page) {
        // TODO: 백엔드A 구현
        // BID 테이블에서 member_id = 나 조건으로 조회
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "입찰 내역 조회 미구현");
    }
}
