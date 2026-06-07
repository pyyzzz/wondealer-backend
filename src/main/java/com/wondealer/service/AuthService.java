package com.wondealer.service;

import com.wondealer.dto.request.LoginReqDto;
import com.wondealer.dto.request.SignUpReqDto;
import com.wondealer.dto.response.MemberResDto;
import com.wondealer.dto.response.TokenDto;
import com.wondealer.entity.Member;
import com.wondealer.entity.RefreshToken;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.MemberRepository;
import com.wondealer.repository.RefreshTokenRepository;
import com.wondealer.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManagerBuilder managerBuilder;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    // ── 회원가입 ──────────────────────────────────────────────────
    public MemberResDto signup(SignUpReqDto dto) {
        // TODO: 백엔드A 구현
        // 1. 이메일/아이디/닉네임 중복 체크
        // 2. dto.toEntity(passwordEncoder)로 Member 생성
        // 3. memberRepository.save() 후 MemberResDto.of() 반환
        // 4. 약관 동의 처리 (TermsAgree INSERT)
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "회원가입 미구현");
    }

    // ── 로그인 ────────────────────────────────────────────────────
    public TokenDto login(LoginReqDto dto) {
        // TODO: 백엔드A 구현
        // 1. dto.toAuthenticationToken()으로 인증 토큰 생성
        // 2. managerBuilder.getObject().authenticate()로 인증
        //    → CustomUserDetailsService.loadUserByUsername() 자동 호출
        // 3. tokenProvider.generateTokenDto()로 JWT 발급
        // 4. Refresh Token DB 저장 (있으면 갱신, 없으면 새로 INSERT)
        // 5. TokenDto에 nickname 담아서 반환
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "로그인 미구현");
    }

    // ── Access Token 재발급 ───────────────────────────────────────
    public TokenDto reissue(String accessToken, String refreshToken) {
        // TODO: 백엔드A 구현
        // 1. tokenProvider.getMemberIdFromToken()으로 만료된 토큰에서 memberId 추출
        // 2. DB에서 Refresh Token 조회
        // 3. savedToken.isExpired() 확인 → 만료 시 로그인 요청
        // 4. 전달받은 refreshToken == DB 저장값 일치 확인 (탈취 방어)
        // 5. 새 Access Token 발급 후 반환
        // 6. (선택) Sliding: Refresh Token도 함께 갱신
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "토큰 재발급 미구현");
    }

    // ── 로그아웃 ──────────────────────────────────────────────────
    public void logout(Long memberId) {
        // TODO: 백엔드A 구현
        // refreshTokenRepository.deleteByMemberId(memberId)
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "로그아웃 미구현");
    }

    // ── 아이디 찾기 ───────────────────────────────────────────────
    public String findUsername(String name, String email) {
        // TODO: 백엔드A 구현
        // 1. name + email로 회원 조회
        // 2. username 마스킹 처리 후 반환 (예: kim***ng)
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "아이디 찾기 미구현");
    }

    // ── 임시 비밀번호 발급 ────────────────────────────────────────
    public void resetPassword(String email) {
        // TODO: 백엔드A 구현
        // 1. email로 회원 조회
        // 2. 임시 비밀번호 생성 (UUID 앞 8자리)
        // 3. BCrypt 암호화 후 MEMBER.password UPDATE
        // 4. JavaMailSender로 이메일 발송
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "임시 비밀번호 발급 미구현");
    }
}
