package com.example.drinkshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_shift")
public class DailyShift {

    @Id
    @Column(name = "business_date")
    private LocalDate businessDate;

    @Column(name = "status", length = 10, nullable = false)
    private String status = "OPEN";

    @Column(name = "opened_by", nullable = false)
    private Long openedBy;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "petty_cash", precision = 10, scale = 2, nullable = false)
    private BigDecimal pettyCash;

    @Column(name = "roles_json", columnDefinition = "json", nullable = false)
    private String rolesJson;

    @Column(name = "revenue_by_payment", columnDefinition = "json")
    private String revenueByPayment;

    @Column(name = "cups")
    private Integer cups;

    @Column(name = "expected_cash", precision = 10, scale = 2)
    private BigDecimal expectedCash;

    @Column(name = "counted_cash", precision = 10, scale = 2)
    private BigDecimal countedCash;

    @Column(name = "diff_reason")
    private String diffReason;

    @Column(name = "closed_by")
    private Long closedBy;

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

