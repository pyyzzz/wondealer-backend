package com.wondealer.security;

import com.wondealer.dto.response.TokenDto;
import com.wondealer.entity.RefreshToken;
import com.wondealer.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // JWT 발급
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
        String accessToken = tokenDto.getAccessToken();

        // RefreshToken DB 저장 (일반 로그인과 동일한 방식)
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long memberId = oAuth2User.getMember().getId();

        refreshTokenRepository.findByMemberId(memberId)
                .ifPresentOrElse(
                        rt -> rt.updateToken(
                                tokenDto.getRefreshToken(),
                                tokenProvider.getRefreshTokenExpiry()),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .memberId(memberId)
                                        .tokenValue(tokenDto.getRefreshToken())
                                        .expiresAt(tokenProvider.getRefreshTokenExpiry())
                                        .build())
                );

        // 프론트 리다이렉트
        String targetUrl = UriComponentsBuilder
                .fromUriString("http://localhost:3000/login-success")
                .queryParam("token", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}