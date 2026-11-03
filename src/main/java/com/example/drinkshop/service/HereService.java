package com.example.drinkshop.service;

import com.example.drinkshop.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 後端呼叫 HERE（地址轉座標、反查地址、機車路線）。使用 pos.here.api-key，這把 key 不會回傳給前端。
 * 起點固定為門市座標 pos.store.lat / pos.store.lng。
 */
@Service
public class HereService {
    private static final Logger log = LoggerFactory.getLogger(HereService.class);
    /** 比對分數低於此值時請店員在地圖上確認位置；用實際地址測試後再調整。 */
    private static final double REVIEW_SCORE = 0.9;
    /** 相同終點的路線在這段時間內直接用快取，節省機車路線額度（每月 5,000 次）。 */
    private static final Duration ROUTE_CACHE = Duration.ofMinutes(30);

    public record GeoResult(BigDecimal lat, BigDecimal lng, String label, boolean needsReview) {}
    public record RouteResult(int distanceM, List<String> polylines) {}
    private record Cached(RouteResult result, Instant at) {}

    private final RestClient http;
    private final String apiKey;
    private final BigDecimal storeLat;
    private final BigDecimal storeLng;
    private final Clock clock;
    private final Map<String, Cached> routes = new ConcurrentHashMap<>();

    public HereService(RestClient.Builder builder, Clock clock,
                       @Value("${pos.here.api-key:}") String apiKey,
                       @Value("${pos.store.lat}") BigDecimal storeLat,
                       @Value("${pos.store.lng}") BigDecimal storeLng) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(8));
        this.http = builder.requestFactory(factory).build();
        this.clock = clock;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.storeLat = storeLat;
        this.storeLng = storeLng;
    }

    /** 地址 → 座標。限台灣、以門市附近優先。找不到時丟 404。 */
    public GeoResult geocode(String address) {
        JsonNode body = call(() -> http.get()
            .uri("https://geocode.search.hereapi.com/v1/geocode?q={q}&in=countryCode:TWN&at={at}&lang=zh-TW&limit=1&apiKey={key}",
                address, storeLat + "," + storeLng, key())
            .retrieve().body(JsonNode.class));
        JsonNode item = body == null ? null : body.path("items").path(0);
        if (item == null || item.isMissingNode() || item.path("position").isMissingNode())
            throw ApiException.missing("地圖找不到這個地址，請在地圖上手動設定位置");
        boolean exact = "houseNumber".equals(item.path("resultType").asText());
        double score = item.path("scoring").path("queryScore").asDouble(0);
        return new GeoResult(coordinate(item.path("position").path("lat")), coordinate(item.path("position").path("lng")),
            item.path("address").path("label").asText(address), !exact || score < REVIEW_SCORE);
    }

    /** 座標 → 地址（調整位置時顯示用）。查不到回傳 null。 */
    public String reverse(BigDecimal lat, BigDecimal lng) {
        JsonNode body = call(() -> http.get()
            .uri("https://revgeocode.search.hereapi.com/v1/revgeocode?at={at}&lang=zh-TW&limit=1&apiKey={key}",
                lat + "," + lng, key())
            .retrieve().body(JsonNode.class));
        JsonNode label = body == null ? null : body.path("items").path(0).path("address").path("label");
        return label == null || label.isMissingNode() ? null : label.asText();
    }

    /** 門市 → 終點的機車路線。 */
    public RouteResult route(BigDecimal lat, BigDecimal lng) {
        String cacheKey = lat.setScale(6, RoundingMode.HALF_UP) + "," + lng.setScale(6, RoundingMode.HALF_UP);
        Instant now = clock.instant();
        Cached cached = routes.get(cacheKey);
        if (cached != null && cached.at().plus(ROUTE_CACHE).isAfter(now)) return cached.result();

        JsonNode body = call(() -> http.get()
            .uri("https://router.hereapi.com/v8/routes?transportMode=scooter&origin={o}&destination={d}&return=polyline,summary&apiKey={key}",
                storeLat + "," + storeLng, lat + "," + lng, key())
            .retrieve().body(JsonNode.class));
        JsonNode route = body == null ? null : body.path("routes").path(0);
        if (route == null || route.isMissingNode())
            throw ApiException.bad("找不到可行的機車路線，請確認送達位置");
        int distance = 0;
        var polylines = new ArrayList<String>();
        for (JsonNode section : route.path("sections")) {
            polylines.add(section.path("polyline").asText());
            distance += section.path("summary").path("length").asInt();
        }
        var result = new RouteResult(distance, List.copyOf(polylines));
        if (routes.size() > 500) routes.clear(); // 一天最多十幾單，簡單限制記憶體即可
        routes.put(cacheKey, new Cached(result, now));
        return result;
    }

    private String key() {
        if (apiKey.isEmpty()) throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "尚未設定 HERE key，請在 .pos-local.properties 設定 HERE_API_KEY");
        return apiKey;
    }

    private JsonNode call(java.util.function.Supplier<JsonNode> request) {
        try {
            return request.get();
        } catch (RestClientResponseException ex) {
            log.warn("HERE API 回應 {}", ex.getStatusCode().value());
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403)
                throw new ApiException(HttpStatus.BAD_GATEWAY, "HERE key 無效或沒有權限，請檢查 HERE_API_KEY");
            if (ex.getStatusCode().value() == 429)
                throw new ApiException(HttpStatus.BAD_GATEWAY, "地圖服務使用量已達上限，請稍後再試");
            throw new ApiException(HttpStatus.BAD_GATEWAY, "地圖服務暫時無法使用，請稍後再試");
        } catch (RestClientException ex) {
            log.warn("HERE API 連線失敗：{}", ex.getClass().getSimpleName());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "無法連線到地圖服務，請確認網路後重試");
        }
    }

    private static BigDecimal coordinate(JsonNode value) {
        return new BigDecimal(value.asText()).setScale(7, RoundingMode.HALF_UP);
    }
}
