package com.wondealer.entity;

import com.wondealer.constant.Authority;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // @CreatedDate, @LastModifiedDate 자동 관리
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(unique = true, nullable = false, length = 50)
    private String username;        // 아이디 (로그인 ID)

    @Column(nullable = false, length = 50)
    private String name;            // 성함

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private String password;        // BCrypt 암호화 저장

    @Column(length = 500)
    private String profileImg;      // S3 이미지 URL

    @Column(length = 20)
    private String phone;

    // ── WonPay 출금 계좌 ─────────────────────────────────────────
    @Column(length = 50)
    private String bankName;        // 은행명 (예: 신한은행)

    @Column(length = 50)
    private String accountNumber;   // 계좌번호 — 화면 표시 시 마스킹 (110-****-5678)

    @Column(length = 50)
    private String accountHolder;   // 예금주명

    // ── 상태 플래그 ───────────────────────────────────────────────
    @Column(nullable = false)
    private boolean isEmailVerified = false;  // false: 미인증 → 상품 등록/입찰 불가

    @Column(nullable = false)
    private boolean isBanned = false;         // true: 정지 → JWT 검증 단계에서 403

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Authority authority;

    // ── 날짜 자동 관리 (@EnableJpaAuditing 필요) ──────────────────
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Member(String email, String username, String name, String nickname,
                  String password, Authority authority,
                  boolean isEmailVerified, boolean isBanned) {
        this.email           = email;
        this.username        = username;
        this.name            = name;
        this.nickname        = nickname;
        this.password        = password;
        this.authority       = authority;
        this.isEmailVerified = isEmailVerified;
        this.isBanned        = isBanned;
    }
}
