package com.example.drinkshop.exception;

import org.springframework.http.HttpStatus;

/**
 * 原料庫存不足例外（HTTP 400）
 * 繼承 ApiException，由全域 ApiExceptionHandler 統一攔截處理
 */
public class InsufficientStockException extends ApiException {

    public InsufficientStockException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

