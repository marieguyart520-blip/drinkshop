package com.example.drinkshop.entity;

import java.util.Optional;

// 員工等級：和資料庫 staff_account.level 的 ENUM 值一樣，由低到高排列
public enum StaffLevel {
    TRAINEE("新進店員"),
    STAFF("正式店員"),
    MANAGER("店長"),
    HQ("總部");

    public final String label;

    StaffLevel(String label) { this.label = label; }

    // 我的等級是否「至少」到達 required（只用來比對飲品的最低可看等級）
    public boolean atLeast(StaffLevel required) { return this.compareTo(required) >= 0; }

    // 資料庫的值轉成等級；空白、拼錯、不認得的值一律回傳 empty（等於沒有任何權限）
    public static Optional<StaffLevel> fromCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        for (StaffLevel l : values()) if (l.name().equals(code.trim())) return Optional.of(l);
        return Optional.empty();
    }
}
