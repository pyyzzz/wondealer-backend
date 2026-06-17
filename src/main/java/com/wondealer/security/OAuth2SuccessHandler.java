package com.wondealer.security;

import com.wondealer.dto.response.TokenDto;
import com.wondealer.service.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // 토큰 DTO 생성 (generateTokenDto 사용)
        // 전 단계에서 조립된 인증 정보(authentication)를 바탕으로 TokenProvider의 generateTokenDto를 실행시켜 JWT 발급
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
        String accessToken = tokenDto.getAccessToken(); // 발급된 엑세스 토큰 문자열 추출

        // React 프론트엔드 서버의 로그인 결과 처리 컴포넌트(/login-success) 주소 빌드
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/login-success")
                .queryParam("token", accessToken)

                .build().toUriString();

        // 빌드 완료된 안전한 타겟 브라우저 URL 주소로 사용자 브라우저를 즉시 강제 리다이렉트(이동)
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}