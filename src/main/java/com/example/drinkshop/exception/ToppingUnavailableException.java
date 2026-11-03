package com.example.drinkshop.exception;

import org.springframework.http.HttpStatus;

/**
 * 加料配料停售或不可使用例外（HTTP 400）
 * 繼承 ApiException，由全域 ApiExceptionHandler 統一攔截處理
 */
public class ToppingUnavailableException extends ApiException {

    public ToppingUnavailableException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

