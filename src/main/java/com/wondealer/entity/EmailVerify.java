package com.wondealer.entity;

import com.wondealer.exception.CustomException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verify")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerify {
    @Id
    @GeneratedValue
    @Column(name = "verify_id")
    private Long id; // PK

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // FK

    @Column(nullable = false)
    private String token;  // 인증 코드 UUID

    @Column(nullable = false)
    private LocalDateTime expiredAt;  // 인증 코드 만료 시간

    @Column(nullable = false)
    @Builder.Default // 인증 성공 시 isUsed = true
    private boolean isUsed = false;  // 처음 생성시 사용안함

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;  // 인증 코드 생성 시간


    public void useToken() {
        if (this.isUsed) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 사용된 토큰입니다.");
        }
        this.isUsed = true;
    }

    public static EmailVerify createEmailVerify(Member member, String token) {
        return EmailVerify.builder()
                .member(member)
                .token(token)
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .isUsed(false)
                .build();
    }

}


