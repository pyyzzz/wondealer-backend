package com.wondealer.repository;

import com.wondealer.entity.WalletCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletChargeRepository extends JpaRepository<WalletCharge, Long> {

    Optional<WalletCharge> findByPaymentId(String paymentId);
}
