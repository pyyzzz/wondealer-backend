package com.wondealer.entity;

// 약관 동의 기록 테이블
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "terms_agree")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) //생성 시간(createdAt)과 수정 시간(updatedAt)을 자동으로 기록
@AllArgsConstructor
@Builder
public class TermsAgree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terms_agree_id")
    private Long id;


    @ManyToOne  // 여러 개의 동의 기록(Many)은 한 명의 회원(One)에게 속해 있다
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    @Column(nullable = false)
    private boolean isAgreed;  // true=이사람이 약관에 동의했다

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime agreedAt;

    public void assignMember(Member member) {
        this.member = member;
    }

}
