package com.wondealer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직에서 발생시키는 커스텀 예외
 * 사용 예: throw new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
 */
@Getter
public class CustomException extends RuntimeException {

    private final HttpStatus status;
    private final String message;

    public CustomException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
}
