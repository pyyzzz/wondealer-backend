package com.wondealer.repository;

import com.wondealer.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // 💡 설계서 스펙 반영: 회원 고유 ID(memberId)를 통해 해당 유저의 지갑을 1:1로 조회
    Optional<Wallet> findByMemberId(Long memberId);
}