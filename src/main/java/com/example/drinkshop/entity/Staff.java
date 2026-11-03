package com.example.drinkshop.entity;

// 由 PosRepository 將資料列轉成員工物件，不是 JPA Entity，不會自動改資料表。
// passwordHash／pinHash 是雜湊與舊版欄位；API 只回傳員工顯示資料，不直接回傳此物件。
public record Staff(long id, String username, String passwordHash, String name,
                    String level, String pinHash, boolean active) {}
