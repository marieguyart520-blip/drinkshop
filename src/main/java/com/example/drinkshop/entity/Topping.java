package com.example.drinkshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 加料配料實體（對應 topping 資料表）
 */
@Entity
@Table(name = "topping")
public class Topping {

    // 加料配料編號（主鍵，自動遞增）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topping_id")
    private Long toppingId;

    // 加料名稱（例如 珍珠、椰果、波霸，唯一值）
    @Column(name = "name", length = 30, nullable = false, unique = true)
    private String name;

    // 顧客加料加購售價
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    // 所關聯的原料庫存編號
    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    // 每份加料需扣除之原料基本單位數量（例如 1 份珍珠扣 50g）
    @Column(name = "deduct_qty", precision = 12, scale = 2, nullable = false)
    private BigDecimal deductQty;

    // 是否在售開放加料（true: 開放中, false: 停售/缺料隱藏）
    @Column(name = "on_sale", nullable = false)
    private Boolean onSale = true;

    public Topping() {
    }

    public Topping(Long toppingId, String name, BigDecimal price, Long inventoryId, BigDecimal deductQty, Boolean onSale) {
        this.toppingId = toppingId;
        this.name = name;
        this.price = price;
        this.inventoryId = inventoryId;
        this.deductQty = deductQty;
        this.onSale = onSale;
    }

    public Long getToppingId() {
        return toppingId;
    }

    public void setToppingId(Long toppingId) {
        this.toppingId = toppingId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public BigDecimal getDeductQty() {
        return deductQty;
    }

    public void setDeductQty(BigDecimal deductQty) {
        this.deductQty = deductQty;
    }

    public Boolean getOnSale() {
        return onSale;
    }

    public void setOnSale(Boolean onSale) {
        this.onSale = onSale;
    }
}
