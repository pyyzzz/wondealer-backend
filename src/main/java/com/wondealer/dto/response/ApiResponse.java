package com.wondealer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 모든 API 응답을 일관된 형식으로 감싸는 공통 wrapper
// 성공: { "success": true, "message": "OK", "data": { ... } }
// 실패: { "success": false, "message": "에러 메시지", "data": null }
@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
