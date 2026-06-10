package com.wondealer.entity;

public enum WalletTxType {
    CHARGE,     // WonPay 충전
    USE,        // 결제 시 잔액 차감 (기존 PAYMENT에서 설계서 명세대로 USE로 수정)
    REFUND,     // 거래 취소로 인한 환불
    WITHDRAW,   // 판매자 대금 출금
    SETTLEMENT  // 거래 완료 후 판매자 정산 지급 (누락 스펙 복구)
}