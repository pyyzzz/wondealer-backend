package com.wondealer.repository;

import com.wondealer.entity.PenaltyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PenaltyLogRepository extends JpaRepository<PenaltyLog, Long> {
    // 회원별 제재 이력 조회 (최신순)
    List<PenaltyLog> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
