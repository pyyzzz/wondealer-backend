package com.wondealer.service;

import com.wondealer.constant.Authority;
import com.wondealer.entity.Member;
import com.wondealer.entity.OauthAccount;
import com.wondealer.repository.MemberRepository;
import com.wondealer.repository.OauthAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final OauthAccountRepository oauthAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 부모 클래스의 loadUser를 호출하여 구글에서 유저 정보를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 디버깅 콘솔용 출력: 구글이 주는 유저 고유 속성들이 다 찍힙니다. (sub, email, name, picture 등)
        System.out.println("구글 사용자 속성: " + oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase(); // "GOOGLE"
        String googleSub = oAuth2User.getAttribute("sub");  // 구글 계정 고유의 고유 ID 스트링
        String email = oAuth2User.getAttribute("email");    // 구글 계정 이메일
        String name = oAuth2User.getAttribute("name");      // 구글 계정 이름

        // 기존에 구글 로그인을 해본 계정인지 DB 조회 후, 없으면 자동으로 회원가입 실행
        OauthAccount oauthAccount = oauthAccountRepository.findByProviderAndProviderId(provider, googleSub)
                .orElseGet(() -> registerNewMember(email, name, provider, googleSub));

        Member member = oauthAccount.getMember();

        // 데이터 정형성 방어 코드: 연동 테이블에는 레코드가 생겼는데 유저 메인 테이블과 맵핑이 끊긴 경우 에러 표출
        if (oauthAccount.getMember() == null) {
            throw new IllegalStateException("회원 정보가 제대로 연결되지 않았습니다.");
        }

        // CustomOAuth2User 반환
        return new CustomOAuth2User(oAuth2User, member);
    }

    // 최초 구글 로그인 시 실행될 신규 회원가입(DB Insert) 내부 메서드
    private OauthAccount registerNewMember(String email, String name, String provider, String providerId) {
        // 1. 이미 존재하는 회원인지 이메일로 확인
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 완전히 처음 보는 유저라면 메인 Member 테이블에 기본값들과 함께 새로 빌드 후 저장
                    Member newMember = Member.builder()
                            .email(email)
                            .name(name)
                            .username(email)
                            .nickname(name)
                            .password("OAUTH_USER") // 소셜 유저용 더미 패스워드 설정
                            .authority(Authority.ROLE_USER)// 기본 일반 권한 부여
                            .isEmailVerified(true)// 구글 메일은 인증된 상태이므로 true 처리
                            .build();
                    return memberRepository.save(newMember);
                });



        // 2. OauthAccount가 이미 있는지 확인 후 저장 (중복 방지)
        return oauthAccountRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    OauthAccount oauth = OauthAccount.builder()
                            .member(member)        // 위에서 가져오거나 생성한 member 객체 연동 (FK)
                            .provider(provider)     // "GOOGLE"
                            .providerId(providerId) // 구글 sub 번호
                            .build();
                    return oauthAccountRepository.save(oauth);
                });
    }
}