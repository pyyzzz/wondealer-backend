package com.wondealer.controller;

import com.wondealer.dto.request.LoginReqDto;
import com.wondealer.dto.request.SignUpReqDto;
import com.wondealer.dto.request.TokenReissueReqDto;
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
    public ResponseEntity<ApiResponse<String>> sendEmailVerification(@RequestParam String email) {
        // 이메일로 가입된 회원 정보를 가져옵니다.
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 이메일로 가입된 회원이 없습니다."));

        // EmailService를 호출하여 토큰 생성 및 메일 발송을 수행합니다.
        emailService.sendVerificationEmail(member);

        return ResponseEntity.ok(ApiResponse.ok("인증 메일이 발송되었습니다.", null));
    }

    // GET /auth/email/verify — 이메일 인증 확인
    @GetMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        // AuthService에 구현된 로직을 호출
        authService.verifyEmail(token);

        // 인증이 성공하면 사용자에게 완료 메시지 보내기
        return ResponseEntity.ok(ApiResponse.ok("이메일 인증이 성공적으로 완료되었습니다.", null));
    }
}
