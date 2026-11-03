package com.example.drinkshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 訂單品項明細資料傳輸物件（扁平化 DTO）
 */
public record OrderItemDto(
        // 明細品項流水號
        Long itemId,

        // 所屬訂單編號
        Long orderId,

        // 商品品項編號
        Long productId,

        // 商品品項名稱
        String productName,

        // 容量大小規格（M/L）
        String size,

        // 溫度（COLD/WARM/HOT）
        String temperature,

        // 甜度設定
        String sugar,

        // 冰塊設定
        String ice,

        // 購買數量/杯數
        Integer qty,

        // 單杯單價（含容量與加料價格）
        BigDecimal unitPrice,

        // 品項製作狀態（PENDING/MAKING/DONE/WASTED 等）
        String status,

        // 客製備註
        String note,

        // 建立時間
        LocalDateTime createdAt
) {}

