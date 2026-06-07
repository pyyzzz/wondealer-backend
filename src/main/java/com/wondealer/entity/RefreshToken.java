package com.wondealer.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;          // 회원당 1개 유지

    @Column(nullable = false)
    private String tokenValue;      // Refresh Token 문자열

    @Column(nullable = false)
    private LocalDateTime expiresAt; // 만료 시각 (7일)

    @Builder
    public RefreshToken(Long memberId, String tokenValue, LocalDateTime expiresAt) {
        this.memberId   = memberId;
        this.tokenValue = tokenValue;
        this.expiresAt  = expiresAt;
    }

    // Sliding Refresh: 재발급 시 토큰 값과 만료 시각 갱신
    public void updateToken(String newTokenValue, LocalDateTime newExpiresAt) {
        this.tokenValue = newTokenValue;
        this.expiresAt  = newExpiresAt;
    }

    // 만료 여부 확인 — AuthService.reissue() 에서 호출
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
