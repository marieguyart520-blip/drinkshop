package com.example.drinkshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @Column(name = "base_unit", length = 10, nullable = false)
    private String baseUnit;

    @Column(name = "pack_unit", length = 10)
    private String packUnit;

    @Column(name = "per_pack", precision = 12, scale = 2)
    private BigDecimal perPack;

    @Column(name = "book_qty", precision = 12, scale = 2, nullable = false)
    private BigDecimal bookQty = BigDecimal.ZERO;

    @Column(name = "safety_stock", precision = 12, scale = 2, nullable = false)
    private BigDecimal safetyStock = BigDecimal.ZERO;

    @Column(name = "reported_qty", precision = 12, scale = 2)
    private BigDecimal reportedQty;

    @Column(name = "reported_by")
    private Long reportedBy;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;

    public Inventory() {
    }

    public Inventory(Long inventoryId, String name, String baseUnit, String packUnit, BigDecimal perPack,
                     BigDecimal bookQty, BigDecimal safetyStock, BigDecimal reportedQty, Long reportedBy,
                     LocalDateTime reportedAt, BigDecimal unitCost) {
        this.inventoryId = inventoryId;
        this.name = name;
        this.baseUnit = baseUnit;
        this.packUnit = packUnit;
        this.perPack = perPack;
        this.bookQty = bookQty;
        this.safetyStock = safetyStock;
        this.reportedQty = reportedQty;
        this.reportedBy = reportedBy;
        this.reportedAt = reportedAt;
        this.unitCost = unitCost;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUnit() {
        return baseUnit;
    }

    public void setBaseUnit(String baseUnit) {
        this.baseUnit = baseUnit;
    }

    public String getPackUnit() {
        return packUnit;
    }

    public void setPackUnit(String packUnit) {
        this.packUnit = packUnit;
    }

    public BigDecimal getPerPack() {
        return perPack;
    }

    public void setPerPack(BigDecimal perPack) {
        this.perPack = perPack;
    }

    public BigDecimal getBookQty() {
        return bookQty;
    }

    public void setBookQty(BigDecimal bookQty) {
        this.bookQty = bookQty;
    }

    public BigDecimal getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(BigDecimal safetyStock) {
        this.safetyStock = safetyStock;
    }

    public BigDecimal getReportedQty() {
        return reportedQty;
    }

    public void setReportedQty(BigDecimal reportedQty) {
        this.reportedQty = reportedQty;
    }

    public Long getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Long reportedBy) {
        this.reportedBy = reportedBy;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}

