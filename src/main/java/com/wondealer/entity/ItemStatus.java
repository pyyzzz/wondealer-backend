package com.wondealer.entity;

public enum ItemStatus {
    SELLING,    // 판매중
    TRADING,    // 거래중 (결제 후 에스크로 상태)
    COMPLETED,  // 거래완료
    DELETED     // 삭제됨 (관리자 또는 판매자 삭제)
}