package com.wondealer.controller;

import com.wondealer.dto.request.AdminBanReqDto;
import com.wondealer.dto.response.AdminItemResDto;
import com.wondealer.dto.response.AdminMemberResDto;
import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.PageResDto;
import com.wondealer.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ── 회원 관리 ─────────────────────────────────────────────────

    @GetMapping("/members")
    public ResponseEntity<ApiResponse<PageResDto<AdminMemberResDto>>> getMembers(
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                PageResDto.from(adminService.getMembers(pageable))));
    }

    @PostMapping("/members/{memberId}/ban")
    public ResponseEntity<ApiResponse<?>> banMember(
            @PathVariable Long memberId,
            @Valid @RequestBody AdminBanReqDto dto) {
        adminService.banMember(memberId, dto);
        return ResponseEntity.ok(ApiResponse.ok("회원이 정지되었습니다.", null));
    }

    @PostMapping("/members/{memberId}/unban")
    public ResponseEntity<ApiResponse<?>> unbanMember(
            @PathVariable Long memberId) {
        adminService.unbanMember(memberId);
        return ResponseEntity.ok(ApiResponse.ok("회원 정지가 해제되었습니다.", null));
    }

    // ── 상품 관리 ─────────────────────────────────────────────────

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<PageResDto<AdminItemResDto>>> getItems(
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                PageResDto.from(adminService.getItems(pageable))));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<?>> deleteItem(
            @PathVariable Long itemId) {
        adminService.deleteItem(itemId);
        return ResponseEntity.ok(ApiResponse.ok("상품이 강제 삭제되었습니다.", null));
    }
}
