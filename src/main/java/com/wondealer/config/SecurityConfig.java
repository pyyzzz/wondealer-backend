package com.wondealer.config;

import com.wondealer.security.*;
import com.wondealer.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandle jwtAccessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;  // 구글 OAuth 유저 처리
    private final OAuth2SuccessHandler oAuth2SuccessHandler;        // 구글 로그인 성공 후 JWT 발급

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // JWT 방식 → 세션 미사용
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증/인가 예외 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401
                        .accessDeniedHandler(jwtAccessDeniedHandler))           // 403

                // 구글 OAuth2 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)) // 유저 정보 처리
                        .successHandler(oAuth2SuccessHandler))         // JWT 발급 및 리다이렉트

                .authorizeHttpRequests(auth -> auth

                        // ── 인증 불필요 ────────────────────────────────────────────
                        .requestMatchers("/auth/**").permitAll()                          // 로그인/회원가입/이메일인증
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()   // 구글 OAuth 콜백
                        .requestMatchers("/login-success").permitAll()                   // OAuth 로그인 후 리다이렉트 페이지
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger UI
                        .requestMatchers("/ws/**").permitAll()                           // WebSocket 연결

                        // ── 비로그인 조회 허용 ──────────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/items/**").permitAll()    // 상품 목록/상세 조회
                        .requestMatchers(HttpMethod.GET, "/api/games/**").permitAll()    // 게임/카테고리/서버 조회
                        .requestMatchers(HttpMethod.GET, "/api/auctions/**").permitAll() // 경매 목록/상세 조회
                        .requestMatchers("/api/rankings").permitAll()                    // 게임 랭킹 조회

                        // ── 관리자 전용 ────────────────────────────────────────────
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ── 나머지 전부 인증 필요 ───────────────────────────────────
                        .anyRequest().authenticated()
                )
                .with(new JwtSecurityConfig(tokenProvider), Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000"); // React 개발 서버
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true); // Authorization 헤더 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}