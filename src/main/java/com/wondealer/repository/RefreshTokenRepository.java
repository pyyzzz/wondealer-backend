package com.wondealer.repository;

import com.wondealer.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByMemberId(Long memberId);

    // 로그아웃 시 Refresh Token 삭제
    void deleteByMemberId(Long memberId);
}
