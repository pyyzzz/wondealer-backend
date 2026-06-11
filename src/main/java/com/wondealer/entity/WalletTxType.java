package com.wondealer.entity;

public enum WalletTxType {
    CHARGE,      // 충전
    USE,         // 결제 (PAYMENT → USE로 변경)
    REFUND,      // 환불
    WITHDRAW,    // 출금
    SETTLEMENT   // 판매자 정산 (거래 완료 후 수수료 차감 후 입금)
}