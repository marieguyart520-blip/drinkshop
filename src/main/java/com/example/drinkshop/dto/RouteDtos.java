package com.example.drinkshop.dto;

import com.example.drinkshop.entity.Member;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public final class RouteDtos {
    private RouteDtos() {}

    /** 臨時終點（「做成備註」）或反查地址用的座標。 */
    public record Point(
        @NotNull @DecimalMin("-90") @DecimalMax("90") @Digits(integer = 3, fraction = 7) BigDecimal lat,
        @NotNull @DecimalMin("-180") @DecimalMax("180") @Digits(integer = 3, fraction = 7) BigDecimal lng) {}

    /**
     * 外送路線。polylines 是 HERE flexible polyline 字串，前端用 H.geo.LineString.fromFlexiblePolyline 解碼。
     * needsReview：這次由地址自動算出的位置不夠精準，請店員在地圖上確認。
     */
    public record DeliveryRoute(BigDecimal lat, BigDecimal lng, Member.GeoSource geoSource, boolean needsReview,
                                String label, int distanceM, List<String> polylines) {}

    public record Address(String label) {}
}
