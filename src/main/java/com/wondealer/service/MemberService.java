package com.wondealer.service;

import com.wondealer.dto.request.UpdateMemberAccountReqDto;
import com.wondealer.dto.response.MemberAccountResDto;
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

        // 수정된 정보를 DTO로 변환하여 반환
        return MemberResDto.of(member);
    }

    // ── 회원 정보 수정 ────────────────────────────────────────────
    @Transactional
    public MemberAccountResDto updateMyInfo(UpdateMemberAccountReqDto dto ) {
        // 1. 현재 로그인한 회원 조회
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        // 2. 유저가 수정을 요청한 필드들만 선택적으로 업데이트
        // DTO의 필드가 null이 아닌 경우에만 변경 -> 기존의 소중한 데이터를 덮어쓰지 않도록 보호
        if (dto.getNickName() != null) member.setNickname(dto.getNickName());
        if (dto.getProfileImg() != null) member.setProfileImg(dto.getProfileImg());
        if (dto.getBankName() != null) member.setBankName(dto.getBankName());
        if (dto.getAccountNumber() != null) member.setAccountNumber(dto.getAccountNumber());
        if (dto.getAccountHolder() != null) member.setAccountHolder(dto.getAccountHolder());

        // 3. 마스킹 처리: 수정된 정보를 바로 DTO로 변환하여 반환
        String masked = maskAccountNumber(member.getAccountNumber());

        // 4. 수정된 정보를 DTO로 변환하여 반환
        return MemberAccountResDto.of(member, masked);
    }

    /**
     * 계좌번호 마스킹: 앞 4자리 노출, 나머지 * 처리
     * 예: 1234567890 -> 1234******
     */
    public String maskAccountNumber(String accountNumber) {
        // 1. 데이터가 없거나 4자리 미만이면 안전하게 마스킹 처리
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }

        String visiblePart = accountNumber.substring(0, 4); // 2. 앞 4자리 추출

        String maskedPart = "*".repeat(accountNumber.length() - 4); // 3. 나머지 길이만큼 * 생성

        return visiblePart + maskedPart;    // 4. 합쳐서 반환
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
        member.setPassword(passwordEncoder.encode(newPassword));
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
