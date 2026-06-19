package com.wondealer.service;

import com.wondealer.constant.Authority;
import com.wondealer.entity.Member;
import com.wondealer.entity.OauthAccount;
import com.wondealer.repository.MemberRepository;
import com.wondealer.repository.OauthAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final OauthAccountRepository oauthAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.debug("구글 사용자 속성: {}", oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration()
                .getRegistrationId().toUpperCase(); // "GOOGLE"
        String googleSub = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        OauthAccount oauthAccount = oauthAccountRepository
                .findByProviderAndProviderId(provider, googleSub)
                .orElseGet(() -> registerNewMember(email, name, provider, googleSub));

        if (oauthAccount.getMember() == null) {
            throw new IllegalStateException("회원 정보가 제대로 연결되지 않았습니다.");
        }

        return new CustomOAuth2User(oAuth2User, oauthAccount.getMember());
    }

    private OauthAccount registerNewMember(String email, String name,
                                           String provider, String providerId) {
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 닉네임 중복 방지: 이름 + 랜덤 4자리
                    String uniqueNickname = name + "_"
                            + UUID.randomUUID().toString().substring(0, 4);

                    Member newMember = Member.builder()
                            .email(email)
                            .name(name)
                            .username(email)
                            .nickname(uniqueNickname)
                            .password(passwordEncoder.encode("OAUTH_USER"))
                            .authority(Authority.ROLE_USER)
                            .isEmailVerified(true)
                            .build();
                    return memberRepository.save(newMember);
                });

        return oauthAccountRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    OauthAccount oauth = OauthAccount.builder()
                            .member(member)
                            .provider(provider)
                            .providerId(providerId)
                            .build();
                    return oauthAccountRepository.save(oauth);
                });
    }
}