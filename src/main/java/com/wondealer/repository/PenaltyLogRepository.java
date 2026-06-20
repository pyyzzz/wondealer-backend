package com.wondealer.repository;

import com.wondealer.entity.PenaltyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PenaltyLogRepository extends JpaRepository<PenaltyLog, Long> {
    // 회원별 제재 이력 조회 (최신순)
    List<PenaltyLog> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}