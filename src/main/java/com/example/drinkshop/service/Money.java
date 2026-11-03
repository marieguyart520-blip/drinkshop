package com.example.drinkshop.service;

import com.example.drinkshop.exception.ApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/** 金額用 BigDecimal 保留兩位小數，對應 MySQL DECIMAL(10,2)，避免浮點數加總誤差。 */
public final class Money {
    public static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal MAX = new BigDecimal("99999999.99");
    private Money() {}

    public static BigDecimal checked(BigDecimal amount) {
        if (amount.signum()<0 || amount.compareTo(MAX)>0)
            throw ApiException.bad("金額必須介於 0 與 99999999.99 元之間");
        try { return amount.setScale(2,RoundingMode.UNNECESSARY); }
        catch (ArithmeticException ex) { throw ApiException.bad("金額最多兩位小數"); }
    }
    public static BigDecimal from(Map<String,Object> row,String key) {
        return checked(new BigDecimal(row.get(key).toString()));
    }
    // 100、100.0、100.00 視為相同金額，避免同筆重試只因格式不同被判定內容變更。
    public static BigDecimal canonical(BigDecimal amount) {
        if(amount==null) return null;
        BigDecimal stripped=amount.stripTrailingZeros();
        return stripped.scale()<0?stripped.setScale(0):stripped;
    }
}
