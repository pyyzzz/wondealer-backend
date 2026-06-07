package com.wondealer.controller;

import com.wondealer.dto.request.LoginReqDto;
import com.wondealer.dto.request.SignUpReqDto;
import com.wondealer.dto.request.TokenReissueReqDto;
import com.wondealer.dto.response.ApiResponse;
import com.wondealer.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /auth/signup — 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(@Valid @RequestBody SignUpReqDto dto) {
        // TODO: 백엔드A 구현
        // authService.signup(dto) 호출 후 201 반환
        return null;
    }

    // POST /auth/login — 로그인 (identifier: username 또는 email)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginReqDto dto) {
        // TODO: 백엔드A 구현
        // authService.login(dto) 호출 후 TokenDto 반환
        return null;
    }

    // POST /auth/reissue — Access Token 재발급
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<?>> reissue(@RequestBody TokenReissueReqDto dto) {
        // TODO: 백엔드A 구현
        // authService.reissue(dto.getAccessToken(), dto.getRefreshToken()) 호출
        return null;
    }

    // POST /auth/logout — 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        // TODO: 백엔드A 구현
        // SecurityUtil.getCurrentMemberId()로 memberId 추출
        // authService.logout(memberId) 호출
        return null;
    }

    // POST /auth/find-username — 아이디 찾기
    @PostMapping("/find-username")
    public ResponseEntity<ApiResponse<?>> findUsername(@RequestBody /* TODO: FindUsernameReqDto */ Object dto) {
        // TODO: 백엔드A 구현
        // name + email 받아서 authService.findUsername() 호출
        // 마스킹된 username 반환 (예: kim***ng)
        return null;
    }

    // POST /auth/reset-password — 임시 비밀번호 발급
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody /* TODO: ResetPasswordReqDto */ Object dto) {
        // TODO: 백엔드A 구현
        // email 받아서 authService.resetPassword() 호출
        // 임시 비밀번호 이메일 발송
        return null;
    }

    // POST /auth/email/send — 이메일 인증 발송
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<?>> sendEmailVerification(@RequestParam String email) {
        // TODO: 백엔드A 구현
        // UUID 토큰 생성 → EMAIL_VERIFY INSERT → JavaMailSender 발송
        return null;
    }

    // GET /auth/email/verify — 이메일 인증 확인
    @GetMapping("/email/verify")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@RequestParam String token) {
        // TODO: 백엔드A 구현
        // token으로 EMAIL_VERIFY 조회 → 만료 확인 → is_email_verified = true
        return null;
    }
}
