package com.example.drinkshop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 通用原料庫存數量請求傳輸物件（扁平化 DTO，用於庫存手動校正、扣減與盤點回報）
 */
public record StockQuantityRequest(
        // 原料數量（必填，大於等於 0，最多 10 位整數 2 位小數）
        @NotNull(message = "數量不能為空")
        @DecimalMin(value = "0", message = "數量不能小於 0")
        @Digits(integer = 10, fraction = 2, message = "數量格式不正確")
        BigDecimal quantity,

        // 經手員工編號（選填，盤點回報時可指定，若未填則自動以當前登入店員為準）
        Long staffId
) {
    public StockQuantityRequest(BigDecimal quantity) {
        this(quantity, null);
    }
}

