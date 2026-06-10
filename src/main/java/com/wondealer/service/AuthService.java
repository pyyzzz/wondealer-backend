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
//    private final WalletRepository walletRepository;


    // ── 회원가입 ──────────────────────────────────────────────────
    public MemberResDto signup(SignUpReqDto dto) {
        // 1-1. 이메일/아이디/닉네임 중복 체크
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다");
        }
        if (memberRepository.existsByUsername(dto.getUsername())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 가입된 아이디입니다");
        }
        if (memberRepository.existsByNickname(dto.getNickname())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 사용중인 닉네임입니다");
        }
        // 2. Member 엔티티 생성 (아직 DB에 저장되지 않은 상태)
        Member member = dto.toEntity(passwordEncoder);

        // 3. 필수 약관 동의 검증
        List<Long> agreedTerms = dto.getTermsAgreed();

        // 3-1. DB에 저장된 모든 필수 약관 리스트 가져오기(isRequired=true)
        List<Terms> requiredTerms = termsRepository.findByIsRequiredTrue();

        // 3-2. 사용자가 필수 약관을 모두 동의했는지 확인
        for (Terms required : requiredTerms) {
            if (agreedTerms == null || !agreedTerms.contains(required.getId())) {
                throw new CustomException(HttpStatus.BAD_REQUEST,
                        required.getTitle() + "에 동의해야 합니다");
            }
        }

        // 3-2. 약관 동의 객체 생성 및 Member와 연결
        if (agreedTerms != null) {
            List<Terms> termsList = termsRepository.findAllById(agreedTerms);
            if (termsList.size() != agreedTerms.size()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "존재하지 않는 약관 ID가 포함되어 있습니다.");
            }
            member.agreeToTerms(termsList); // Member 엔티티 내부에서 TermsAgree 생성 및 추가
        }

        // 4. 최종 저장 (CascadeType.ALL 덕분에 member와 termsAgree가 한 번에 저장됨)
        memberRepository.save(member);

//        // 5. 회원가입 시 WALLET 자동 생성
//        Wallet wallet = Wallet.createWallet(member);
//        walletRepository.save(wallet);

        return MemberResDto.of(member);
    }

    // ── 로그인 ────────────────────────────────────────────────────
    public TokenDto login(LoginReqDto dto) {
        // 1. identifier(이메일 또는 아이디)로 회원 조회
        Member member = memberRepository.findByEmail(dto.getIdentifier())  // 이메일로 먼저 찾아보고
                .or(() -> memberRepository.findByUsername(dto.getIdentifier()))  // 없으면 아이디로 찾아봄
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "가입되지 않은 정보입니다."));

        // 2. 차단 여부 체크
        if (member.isBanned()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "차단된 계정입니다. 관리자에게 문의하세요.");
        }

        // 3. 인증 매니저에게 '이 이메일(member.getEmail())의 패스워드를 검증해줘'라고 명확히 지시
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(member.getEmail(), dto.getPassword());

        // 4. managerBuilder.getObject().authenticate()로 인증
        //    → CustomUserDetailsService.loadUserByUsername() 자동 호출
        Authentication authentication = managerBuilder.getObject().authenticate(authenticationToken);

        // 5. tokenProvider.generateTokenDto()로 JWT 발급
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 6. Refresh Token DB 저장 (있으면 갱신, 없으면 새로 INSERT)
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

        // 5. TokenDto에 nickname 담아서 반환
        tokenDto.setNickname(member.getNickname());

        return tokenDto;
    }

    // ── Access Token 재발급 ───────────────────────────────────────
    public TokenDto reissue(String accessToken, String refreshToken) {
        // 1. tokenProvider.getMemberIdFromToken()으로 만료된 토큰에서 memberId 추출
        // Access Token이 만료되어도 토큰 내부의 사용자 정보(Claim)는 추출 가능해야 함
        Long memberId = tokenProvider.getMemberIdFromToken(accessToken);

        // 2. DB에서 Refresh Token 조회
        // 해당 사용자가 가지고 있는 가장 최신의 Refresh Token을 DB에서 확인
        RefreshToken savedToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));

        // 3. savedToken.isExpired() 확인 → 만료 시 로그인 요청
        // 토큰의 유효 기간이 지났는지 확인 (시간적 보안 검증)
        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(savedToken); // 만료된 토큰은 삭제
            throw new CustomException(HttpStatus.UNAUTHORIZED, "세션이 만료되었습니다. 다시 로그인해주세요.");
        }

        // 4. 전달받은 refreshToken == DB 저장값 일치 확인 (탈취 방어)
        // 클라이언트가 보낸 토큰과 DB 값이 다른 경우, 토큰 탈취 혹은 중복 로그인 시도로 간주하여 차단
        if (!savedToken.getTokenValue().equals(refreshToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "토큰 정보가 일치하지 않습니다.");
        }

        // 5. 새 Access Token 발급 후 반환
        // 검증 완료 시, 기존 인증 정보를 바탕으로 새로운 토큰 세트(Access + Refresh)를 생성
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(String.valueOf(memberId));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 6. (선택) Sliding: Refresh Token도 함께 갱신
        // 사용자가 활동 중이라면 Refresh Token의 유효 기간을 다시 7일로 연장 (Sliding Window 기법)
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
        // TODO: 백엔드A 구현
        // 1. name + email로 회원 조회
        // 2. username 마스킹 처리 후 반환 (예: kim***ng)
        throw new CustomException(HttpStatus.NOT_IMPLEMENTED, "아이디 찾기 미구현");
    }

    // ── 임시 비밀번호 발급 ────────────────────────────────────────
    public void resetPassword(String email) {
        // 1. email로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 이메일로 가입된 회원이 없습니다."));

        // 2. 임시 비밀번호 생성 (UUID 앞 8자리)
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        // 3. BCrypt 암호화 후 MEMBER.password UPDATE
        member.setPassword(passwordEncoder.encode(tempPassword));
        memberRepository.save(member);

        // 4. JavaMailSender로 이메일 발송
        String subject = "[WonDealer] 임시 비밀번호 안내";
        String text = "요청하신 임시 비밀번호는 " + tempPassword + " 입니다.\n로그인 후 반드시 비밀번호를 변경해 주세요.";

        emailService.sendEmail(email, subject, text);
    }

    // ── 사용자가 요청한 이메일 주소가 우리 서비스에
    // 실제로 존재하는지 확인하고, 존재한다면 인증 프로세스를 시작 ────────────────────────────────────────
    public void sendVerificationEmail(String email) {
        // 1. 회원 조회 (로직을 서비스로 이동)
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 이메일로 가입된 회원이 없습니다."));

        // 2. 이메일 발송 서비스 호출
        emailService.sendVerificationEmail(member);
    }



    // ── 사용자가 이메일 인증 링크를 클릭했을 때,
    // 서버가 인증을 완료하고 회원의 가입 상태를 '인증 완료'로 변경 ────────────────────────────────────────
    public void verifyEmail(String token) {
        // 1. 토큰으로 EMAIL_VERIFY 조회
        EmailVerify emailVerify = emailVerifyRepository.findByToken(token)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 토큰입니다."));


        // 2. 토큰 만료 시간 확인 (현재 시간과 비교)
        if (emailVerify.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "인증 시간이 만료되었습니다. 다시 요청해주세요.");
        }
        // 3. 사용 여부 검증 및 상태 변경 (엔티티의 책임)
        emailVerify.useToken();

        // 4. 회원 상태 변경 (is_email_verified = true)
        Member member = emailVerify.getMember();
        member.setEmailVerified(true);
    }
}
