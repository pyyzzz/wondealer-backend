package com.wondealer.repository;

import com.wondealer.entity.EmailVerify;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerifyRepository extends JpaRepository<EmailVerify, Long> {
    Optional<EmailVerify> findByToken(String token);

    // 가장 최근에 보낸(생성된) 인증번호 딱 1개만 가져오도록 수정
    Optional<EmailVerify> findTopByEmailOrderByCreatedAtDesc(String email);
}
