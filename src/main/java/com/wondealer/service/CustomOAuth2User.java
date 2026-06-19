package com.wondealer.service;

import com.wondealer.entity.Member;
import lombok.Getter;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {
    // 우리 애플리케이션의 실제 DB 엔티티인 Member 객체를 통째로 품고 다닐 수 있게 필드로 추가
    private final Member member;

    // 구글이 준 원본 속성들과 권한 정보들을 부모 클래스(DefaultOAuth2User)에 상속 전달하며 세팅
    public CustomOAuth2User(OAuth2User oAuth2User, Member member) {
        // "sub" 값을 고유 식별자 키값 명칭으로 지정하여 부모 객체 생성
        super(oAuth2User.getAuthorities(), oAuth2User.getAttributes(), "sub");
        this.member = member;
    }

    @Override
    public String getName() {
        // 구글이 주는 긴 숫자(sub) 대신, 우리 DB의 PK인 memberId를 반환합니다.
        // 이렇게 하면 NumberFormatException 에러가 해결됩니다.
        return member.getId().toString();
    }


}