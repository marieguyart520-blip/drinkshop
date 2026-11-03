package com.example.drinkshop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    private final String hereMapKey;
    private final BigDecimal storeLat;
    private final BigDecimal storeLng;

    public ConfigController(@Value("${pos.here.map-key:}") String hereMapKey,
            @Value("${pos.store.lat}") BigDecimal storeLat,
            @Value("${pos.store.lng}") BigDecimal storeLng) {
        this.hereMapKey = hereMapKey;
        this.storeLat = storeLat;
        this.storeLng = storeLng;
    }

    public record Store(BigDecimal lat, BigDecimal lng) {
    }

    public record Response(String hereMapKey, Store store) {
    }

    @GetMapping
    Response config() {
        return new Response(hereMapKey, new Store(storeLat, storeLng));
    }
}