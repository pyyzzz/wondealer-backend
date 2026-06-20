package com.wondealer.service;

import com.wondealer.dto.request.AdminBanReqDto;
import com.wondealer.dto.response.AdminItemResDto;
import com.wondealer.dto.response.AdminMemberResDto;
import com.wondealer.entity.Item;
import com.wondealer.entity.Member;
import com.wondealer.entity.PenaltyLog;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.ItemRepository;
import com.wondealer.repository.MemberRepository;
import com.wondealer.repository.PenaltyLogRepository;
import com.wondealer.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final PenaltyLogRepository penaltyLogRepository;

    // ── 전체 회원 목록 조회 ───────────────────────────────────────
    public Page<AdminMemberResDto> getMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(AdminMemberResDto::from);
    }

    // ── 회원 정지 ─────────────────────────────────────────────────
    @Transactional
    public void banMember(Long memberId, AdminBanReqDto dto) {
        Long adminId = SecurityUtil.getCurrentMemberId();
        Member admin = findMember(adminId);
        Member target = findMember(memberId);

        if (target.isBanned()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 정지된 회원입니다.");
        }

        target.ban();

        // 제재 이력 저장
        penaltyLogRepository.save(PenaltyLog.builder()
                .member(target)
                .admin(admin)
                .reason(dto.getReason())
                .build());
    }

    // ── 회원 정지 해제 ────────────────────────────────────────────
    @Transactional
    public void unbanMember(Long memberId) {
        Member target = findMember(memberId);

        if (!target.isBanned()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "정지 상태가 아닌 회원입니다.");
        }

        target.unban();
    }

    // ── 전체 상품 목록 조회 ───────────────────────────────────────
    public Page<AdminItemResDto> getItems(Pageable pageable) {
        return itemRepository.findAll(pageable)
                .map(AdminItemResDto::from);
    }

    // ── 상품 강제 삭제 (소프트 삭제) ─────────────────────────────
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (item.isDeletedByAdmin()) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST, "이미 삭제된 상품입니다.");
        }

        item.deleteByAdmin();
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
