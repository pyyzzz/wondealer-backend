package com.wondealer.entity;

public enum ItemStatus {
    SELLING,    // 판매중 (기존 FOR_SALE에서 설계서 스펙으로 수정)
    RESERVED,   // 예약중
    COMPLETED,  // 거래완료 (기존 SOLD_OUT에서 설계서 스펙으로 수정)
    DELETED     // 삭제됨 (누락된 소프트 딜리트 상태 추가)
}