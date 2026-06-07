package com.wondealer.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public class SecurityUtil {

    private SecurityUtil() {}  // 인스턴스 생성 방지 (static 유틸 클래스)

    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("인증 정보가 없습니다. 로그인 후 이용해주세요.");
        }

        return Long.parseLong(authentication.getName());
    }
}
