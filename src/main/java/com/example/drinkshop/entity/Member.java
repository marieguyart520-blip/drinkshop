package com.example.drinkshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
public class Member {
    public enum GeoSource { GEOCODE, MANUAL }

    @Id
    @Column(length = 20)
    private String phone; // 主鍵是電話，由店員輸入，不自動產生

    @Column(nullable = false, length = 50)
    private String name;

    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(precision = 10, scale = 7)
    private BigDecimal lng;

    /** 座標來源：GEOCODE 由 HERE 自動計算（每日清除），MANUAL 由店員手動設定。沒有座標時為 null。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "geo_source")
    private GeoSource geoSource;

    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt; // 交給資料庫自動填

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected Member() {} // JPA 需要無參數建構子

    public Member(String phone, String name) {
        this.phone = phone;
        this.name = name;
    }

    public String getPhone() { return phone; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public GeoSource getGeoSource() { return geoSource; }
    public void setGeoSource(GeoSource geoSource) { this.geoSource = geoSource; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
