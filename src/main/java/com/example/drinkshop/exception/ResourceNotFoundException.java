package com.example.drinkshop.exception;

import org.springframework.http.HttpStatus;

/**
 * 查無指定資源例外（HTTP 404）
 * 繼承 ApiException，由全域 ApiExceptionHandler 統一攔截處理
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}

