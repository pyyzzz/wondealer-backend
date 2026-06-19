package com.wondealer.service;

import com.wondealer.dto.request.UpdateBankReqDto;
import com.wondealer.dto.request.UpdateMemberInfoReqDto;
import com.wondealer.dto.response.MemberResDto;
import com.wondealer.entity.Member;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.MemberRepository;
import com.wondealer.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
    public MemberResDto updateMyInfo(UpdateMemberInfoReqDto dto ) {
        // 1. 현재 로그인한 사용자 식별
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 2. DB에서 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        // 3. 닉네임 수정 (엔티티 메서드 호출)
        member.updateMemberInfo(dto.getNickname(), dto.getPhone());

        // 4. 수정된 정보를 ResDto로 변환하여 반환}
        // 프로필 이미지 업데이트 (Firebase URL)
        if (dto.getProfileImg() != null && !dto.getProfileImg().isBlank()) {
            member.updateProfileImg(dto.getProfileImg());
        }


        return MemberResDto.of(member);
    }

    // ── 비밀번호 변경 ─────────────────────────────────────────────
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        // 1. 현재 로그인한 회원 조회
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        // 1. 현재 비밀번호 BCrypt 검증
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다");
        }
        // 2. 새 비밀번호 암호화 후 UPDATE
        member.changePassword(passwordEncoder.encode(newPassword));
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