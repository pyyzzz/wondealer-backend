package com.wondealer.repository;

import com.wondealer.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByMemberId(Long memberId);

    // 잔액 차감 — DB 레벨 원자적 처리 (동시성 보장)
    // 반환값: 1 = 성공, 0 = 잔액 부족
    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance - :amount " +
            "WHERE w.member.id = :memberId AND w.balance >= :amount")
    int deductBalance(@Param("memberId") Long memberId,
                      @Param("amount") Long amount);
}