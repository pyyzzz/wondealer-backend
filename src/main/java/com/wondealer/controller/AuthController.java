package com.wondealer.controller;

import com.wondealer.dto.request.*;
import com.wondealer.dto.response.ApiResponse;
import com.wondealer.dto.response.MemberResDto;
import com.wondealer.dto.response.TokenDto;
import com.wondealer.entity.Member;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.MemberRepository;
import com.wondealer.service.AuthService;
import com.wondealer.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberRepository memberRepository;
    private final EmailService emailService;

    // POST /auth/signup — 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResDto>> signup(@Valid @RequestBody SignUpReqDto dto) {
        MemberResDto memberResDto = authService.signup(dto);
        // authService.signup(dto) 호출 후 201 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("회원가입이 완료되었습니다.", memberResDto));
    }

    // POST /auth/login — 로그인 (identifier: username 또는 email)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDto>> login(@RequestBody LoginReqDto dto) {
        // TODO: 백엔드A 구현
        // authService.login(dto) 호출 후 TokenDto 반환
        TokenDto tokenDto = authService.login(dto);

        // 2. ApiResponse<TokenDto> 타입으로 응답 반환
        return ResponseEntity.ok(ApiResponse.ok("로그인 성공", tokenDto));
    }

    // POST /auth/reissue — Access Token 재발급
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenDto>> reissue(@RequestBody TokenReissueReqDto dto) {
        // authService.reissue(dto.getAccessToken(), dto.getRefreshToken()) 호출
        TokenDto tokenDto = authService.reissue(dto.getAccessToken(), dto.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.ok("토큰이 재발급되었습니다", tokenDto));
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
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordReqDto dto) {
        // email 받아서 authService.resetPassword() 호출
        authService.resetPassword(dto.getEmail());
        // 임시 비밀번호 이메일 발송
        return ResponseEntity.ok(ApiResponse.ok("임시 비밀번호가 이메일로 발송되었습니다.", null));
    }

    // POST /auth/email/send — 이메일 인증 발송
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<String>> sendEmailVerification(@RequestBody EmailReqDto dto) {
        // 1. 비즈니스 로직 위임: 서비스 계층에서 회원 존재 여부 확인 및 이메일 발송 처리
        authService.sendVerificationEmail(dto.getEmail());

        // 2. 결과 응답: 인증 메일 발송 성공 안내
        return ResponseEntity.ok(ApiResponse.ok("인증 메일이 발송되었습니다.", null));
    }

    // POST /auth/email/verify — 이메일 인증 확인
    // RequestBody로 바꾸면서 POST로 써야해서 중간 다리 역할(프론트엔드 페이지)이 하나 추가해야함.
    // EmailVerifyPage.js를 만들어야함
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestBody EmailVerifyReqDto dto) {
        // 1. 비즈니스 로직 위임: 서비스 계층에서 토큰 검증 및 회원 상태 업데이트 처리
        authService.verifyEmail(dto.getToken());

        // 인증이 성공하면 사용자에게 완료 메시지 보내기
        return ResponseEntity.ok(ApiResponse.ok("이메일 인증이 성공적으로 완료되었습니다.", null));
    }
}
