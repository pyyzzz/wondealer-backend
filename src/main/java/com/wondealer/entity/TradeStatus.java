package com.wondealer.entity;

public enum TradeStatus {
    PENDING_PAYMENT,  // 결제 대기 (PAY_WAITING → PENDING_PAYMENT)
    PAID,             // 결제 완료 (에스크로)
    COMPLETED,        // 거래 확정
    CANCELED         // 거래 취소 (CANCELED)
}