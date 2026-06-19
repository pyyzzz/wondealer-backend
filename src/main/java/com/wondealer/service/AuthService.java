package com.wondealer.service;

import com.wondealer.dto.request.LoginReqDto;
import com.wondealer.dto.request.SignUpReqDto;
import com.wondealer.dto.response.MemberResDto;
import com.wondealer.dto.response.TokenDto;
import com.wondealer.entity.*;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.*;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManagerBuilder managerBuilder;
    private final MemberRepository memberRepository;
    private final TermsRepository termsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final EmailVerifyRepository emailVerifyRepository;
    private final EmailService emailService;
    private final WalletRepository walletRepository;

    // ── 회원가입 ──────────────────────────────────────────────────
    public MemberResDto signup(SignUpReqDto dto) {

        // 1. 이메일 인증 완료 여부 확인 (회원가입 전 필수)
        EmailVerify emailVerify = emailVerifyRepository
                .findTopByEmailOrderByCreatedAtDesc(dto.getEmail())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.BAD_REQUEST, "이메일 인증을 먼저 진행해주세요."));

        if (!emailVerify.isUsed()) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다.");
        }

        // 2. 이메일/아이디/닉네임 중복 체크
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다.");
        }
        if (memberRepository.existsByUsername(dto.getUsername())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 가입된 아이디입니다.");
        }
        if (memberRepository.existsByNickname(dto.getNickname())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 사용중인 닉네임입니다.");
        }

        // 3. Member 엔티티 생성
        Member member = dto.toEntity(passwordEncoder);
        member.verifyEmail(); // 이미 인증됐으므로 바로 true 처리

        // 4. 필수 약관 동의 검증
        List<Long> agreedTerms = dto.getTermsAgreed();
        List<Terms> requiredTerms = termsRepository.findByIsRequiredTrue();

        for (Terms required : requiredTerms) {
            if (agreedTerms == null || !agreedTerms.contains(required.getId())) {
                throw new CustomException(HttpStatus.BAD_REQUEST,
                        required.getTitle() + "에 동의해야 합니다.");
            }
        }

        if (agreedTerms != null) {
            List<Terms> termsList = termsRepository.findAllById(agreedTerms);
            if (termsList.size() != agreedTerms.size()) {
                throw new CustomException(
                        HttpStatus.BAD_REQUEST, "존재하지 않는 약관 ID가 포함되어 있습니다.");
            }
            member.agreeToTerms(termsList);
        }

        // 5. Member 저장
        memberRepository.save(member);

        // 6. WonPay 지갑 자동 생성
        walletRepository.save(Wallet.builder()
                .member(member)
                .build());

        return MemberResDto.of(member);
    }

    // ── 로그인 ────────────────────────────────────────────────────
    public TokenDto login(LoginReqDto dto) {
        Member member = memberRepository.findByEmail(dto.getIdentifier())
                .or(() -> memberRepository.findByUsername(dto.getIdentifier()))
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND, "가입되지 않은 정보입니다."));

        if (member.isBanned()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "차단된 계정입니다. 관리자에게 문의하세요.");
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(member.getEmail(), dto.getPassword());

        Authentication authentication = managerBuilder.getObject().authenticate(authenticationToken);
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(member.getId())
                .map(token -> {
                    token.updateToken(tokenDto.getRefreshToken(), LocalDateTime.now().plusDays(7));
                    return token;
                })
                .orElse(RefreshToken.builder()
                        .memberId(member.getId())
                        .tokenValue(tokenDto.getRefreshToken())
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .build());

        refreshTokenRepository.save(refreshToken);
        tokenDto.setNickname(member.getNickname());

        if (member.getAuthority() != null) {
            tokenDto.setRole(member.getAuthority().name());
        }

        return tokenDto;
    }

    // ── Access Token 재발급 ───────────────────────────────────────
    public TokenDto reissue(String accessToken, String refreshToken) {
        Long memberId = tokenProvider.getMemberIdFromToken(accessToken);

        RefreshToken savedToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));

        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(savedToken);
            throw new CustomException(
                    HttpStatus.UNAUTHORIZED, "세션이 만료되었습니다. 다시 로그인해주세요.");
        }

        if (!savedToken.getTokenValue().equals(refreshToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "토큰 정보가 일치하지 않습니다.");
        }

        UserDetails userDetails = customUserDetailsService
                .loadUserByUsername(String.valueOf(memberId));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
        savedToken.updateToken(tokenDto.getRefreshToken(), tokenProvider.getRefreshTokenExpiry());
        refreshTokenRepository.save(savedToken);

        return tokenDto;
    }

    // ── 로그아웃 ──────────────────────────────────────────────────
    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    // ── 아이디 찾기 ───────────────────────────────────────────────
    public String findUsername(String name, String email) {
        Member member = memberRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND, "일치하는 회원 정보를 찾을 수 없습니다."));
        return maskUsername(member.getUsername());
    }

    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) return username;
        int len = username.length();
        int maskLen = len / 2;
        String prefix = username.substring(0, (len - maskLen) / 2);
        String suffix = username.substring(len - (len - maskLen) / 2);
        return prefix + "*".repeat(maskLen) + suffix;
    }

    // ── 임시 비밀번호 발급 ────────────────────────────────────────
    public void resetPassword(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND, "해당 이메일로 가입된 회원이 없습니다."));

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        member.changePassword(passwordEncoder.encode(tempPassword));
        memberRepository.save(member);

        String subject = "[원딜러] 임시 비밀번호 안내";
        String text = "요청하신 임시 비밀번호는 <b>" + tempPassword
                + "</b> 입니다.<br>로그인 후 반드시 비밀번호를 변경해 주세요.";
        emailService.sendEmail(email, subject, text);
    }

    // ── 회원가입 전 이메일 인증 발송 ─────────────────────────────
    // 회원가입 페이지에서 "인증 메일 발송" 버튼 클릭 시 호출
    public void sendVerificationEmail(String email) {
        // 이미 가입된 이메일이면 발송 불가
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }
        // EmailVerify 저장 + 인증 링크 메일 발송
        emailService.sendVerificationEmail(email);
    }

    // ── 이메일 인증 링크 클릭 처리 ───────────────────────────────
    // 인증 링크 클릭 시 호출 → EmailVerify.isUsed = true
    public void verifyEmail(String token) {
        EmailVerify emailVerify = emailVerifyRepository.findByToken(token)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.BAD_REQUEST, "유효하지 않은 인증 토큰입니다."));

        if (emailVerify.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST, "인증 시간이 만료되었습니다. 다시 요청해주세요.");
        }

        // 인증 완료 처리 (Member 없어도 됨 - 회원가입 전이므로)
        emailVerify.useToken();
    }
}