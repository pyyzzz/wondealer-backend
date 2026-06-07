package com.wondealer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginReqDto {

    // username 또는 email 둘 다 허용
    // CustomUserDetailsService에서 findByEmailOrUsername() 로 조회
    private String identifier;
    private String password;

    // Spring Security 인증 처리 시작점 — AuthenticationManager가 이 토큰을 받아 검증
    public UsernamePasswordAuthenticationToken toAuthenticationToken() {
        return new UsernamePasswordAuthenticationToken(identifier, password);
    }
}
