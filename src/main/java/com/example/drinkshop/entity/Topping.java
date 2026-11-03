package com.example.drinkshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "topping")
public class Topping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topping_id")
    private Long toppingId;

    @Column(name = "name", length = 30, nullable = false, unique = true)
    private String name;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "deduct_qty", precision = 12, scale = 2, nullable = false)
    private BigDecimal deductQty;

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

