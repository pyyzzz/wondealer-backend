package com.wondealer.entity;

public enum TradeStatus {
    PENDING_PAYMENT, // 결제 대기
    PAID,        // 결제 완료 (에스크로 보관)
    COMPLETED,   // 거래 최종 확정 (정산 완료)
    CANCELED     // 거래 취소
}