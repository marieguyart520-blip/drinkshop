package com.example.drinkshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日班次與日結實體（對應 daily_shift 資料表）
 */
@Entity
@Table(name = "daily_shift")
public class DailyShift {

    // 營業日期（主鍵）
    @Id
    @Column(name = "business_date")
    private LocalDate businessDate;

    // 班次狀態（OPEN: 開班中, CLOSED: 已結班）
    @Column(name = "status", length = 10, nullable = false)
    private String status = "OPEN";

    // 開班人員員工編號
    @Column(name = "opened_by", nullable = false)
    private Long openedBy;

    // 開班時間
    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    // 開班零用金 / 備用金
    @Column(name = "petty_cash", precision = 10, scale = 2, nullable = false)
    private BigDecimal pettyCash;

    // 當日員工排班崗位與角色（JSON 格式字串）
    @Column(name = "roles_json", columnDefinition = "json", nullable = false)
    private String rolesJson;

    // 各支付方式營收統計（JSON 格式字串）
    @Column(name = "revenue_by_payment", columnDefinition = "json")
    private String revenueByPayment;

    // 當日總銷售杯數
    @Column(name = "cups")
    private Integer cups;

    // 系統應有現金金額（零用金 + 現金營收 - 現金退款）
    @Column(name = "expected_cash", precision = 10, scale = 2)
    private BigDecimal expectedCash;

    // 關班實際清點現金金額
    @Column(name = "counted_cash", precision = 10, scale = 2)
    private BigDecimal countedCash;

    // 現金短溢差異原因說明
    @Column(name = "diff_reason")
    private String diffReason;

    // 關班結算人員員工編號
    @Column(name = "closed_by")
    private Long closedBy;

    // 關班結算時間
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public DailyShift() {
    }

    public DailyShift(LocalDate businessDate, String status, Long openedBy, LocalDateTime openedAt,
                      BigDecimal pettyCash, String rolesJson, String revenueByPayment, Integer cups,
                      BigDecimal expectedCash, BigDecimal countedCash, String diffReason,
                      Long closedBy, LocalDateTime closedAt) {
        this.businessDate = businessDate;
        this.status = status;
        this.openedBy = openedBy;
        this.openedAt = openedAt;
        this.pettyCash = pettyCash;
        this.rolesJson = rolesJson;
        this.revenueByPayment = revenueByPayment;
        this.cups = cups;
        this.expectedCash = expectedCash;
        this.countedCash = countedCash;
        this.diffReason = diffReason;
        this.closedBy = closedBy;
        this.closedAt = closedAt;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getOpenedBy() {
        return openedBy;
    }

    public void setOpenedBy(Long openedBy) {
        this.openedBy = openedBy;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public BigDecimal getPettyCash() {
        return pettyCash;
    }

    public void setPettyCash(BigDecimal pettyCash) {
        this.pettyCash = pettyCash;
    }

    public String getRolesJson() {
        return rolesJson;
    }

    public void setRolesJson(String rolesJson) {
        this.rolesJson = rolesJson;
    }

    public String getRevenueByPayment() {
        return revenueByPayment;
    }

    public void setRevenueByPayment(String revenueByPayment) {
        this.revenueByPayment = revenueByPayment;
    }

    public Integer getCups() {
        return cups;
    }

    public void setCups(Integer cups) {
        this.cups = cups;
    }

    public BigDecimal getExpectedCash() {
        return expectedCash;
    }

    public void setExpectedCash(BigDecimal expectedCash) {
        this.expectedCash = expectedCash;
    }

    public BigDecimal getCountedCash() {
        return countedCash;
    }

    public void setCountedCash(BigDecimal countedCash) {
        this.countedCash = countedCash;
    }

    public String getDiffReason() {
        return diffReason;
    }

    public void setDiffReason(String diffReason) {
        this.diffReason = diffReason;
    }

    public Long getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(Long closedBy) {
        this.closedBy = closedBy;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
