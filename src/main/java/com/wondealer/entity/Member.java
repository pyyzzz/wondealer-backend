package com.wondealer.entity;

import com.wondealer.constant.Authority;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TermsAgree> termsAgrees = new ArrayList<>();

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

    // 리스트를 순회하며 동의 객체를 생성함 - 약관 동의 작업을 수행하는 절차
    public void agreeToTerms(List<Terms> termsList) {
        for (Terms terms : termsList) {
            // TermsAgree 객체 생성 및 리스트 추가
            TermsAgree agree = TermsAgree.builder()
                    .member(this)
                    .terms(terms)
                    .build();

            // 연관관계 편의 메서드 호출
            this.addTermsAgree(agree);
        }
    }

    // 생성된 객체를 리스트에 넣고 부모 정보를 세팅 - 데이터가 꼬이지 않게 양쪽을 정확히 연결해주는 절차
    public void addTermsAgree(TermsAgree termsAgree) {
        this.termsAgrees.add(termsAgree); // 리스트에 추가

        // TermsAgree 쪽에도 현재 Member를 설정 (양방향 연결)
        if (termsAgree.getMember() != this) {
            termsAgree.assignMember(this);
        }
    }

    // 이메일 인증 완료
    public void verifyEmail() {
        this.isEmailVerified = true;
    }

    // 비밀번호 변경
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 프로필 수정
    public void updateProfile(String nickname, String profileImg) {
        if (nickname != null) this.nickname = nickname;
        if (profileImg != null) this.profileImg = profileImg;
    }

    // 계좌 정보 수정
    public void updateBankInfo(String bankName, String accountNumber, String accountHolder) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
    }

    // 회원 정지/해제 (관리자)
    public void ban() { this.isBanned = true; }
    public void unban() { this.isBanned = false; }

}