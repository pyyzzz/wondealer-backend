package com.wondealer.entity;

public enum TradeStatus {
    PAY_WAITING, // 결제 대기
    PAID,        // 결제 완료 (에스크로 보관)
    SHIPPED,     // 물품 인계 완료
    COMPLETED,   // 거래 최종 확정 (정산 완료)
    CANCELED     // 거래 취소
}