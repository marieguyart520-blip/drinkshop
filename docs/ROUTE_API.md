# 外送路線 API（HERE）

所有路徑都需要員工登入。HERE 查詢用的 key（`HERE_API_KEY`）只在後端；前端顯示地圖用的 key 由 `GET /api/config` 在登入後提供。
起點固定為門市座標 `pos.store.lat` / `pos.store.lng`，載具固定機車（`transportMode=scooter`）。

| 方法 | 路徑 | 說明 |
|---|---|---|
| GET | `/api/members/{phone}/route` | 會員外送路線。會員沒有座標時，用地址向 HERE 計算並存回（`geoSource=GEOCODE`）。 |
| POST | `/api/route` | body `{ "lat": 22.6, "lng": 120.3 }`，任意終點的路線（「只用於這張單」）。 |
| POST | `/api/geo/reverse` | body 同上，回傳 `{ "label": "地址" }`，查不到時 label 為 null。 |

路線回應：

```json
{
  "lat": 22.6123456, "lng": 120.3012345, "geoSource": "GEOCODE",
  "needsReview": false, "label": "台灣高雄市前鎮區…", "distanceM": 2350,
  "polylines": ["BFoz5xJ67i1B1B7PzIhaxL7Y"]
}
```

- `needsReview=true`：這次由地址算出的位置沒有精準到門牌（或比對分數低於 0.9），前端會自動打開調整位置的地圖。
- `polylines` 是 HERE flexible polyline，前端用 `H.geo.LineString.fromFlexiblePolyline` 解碼。
- 相同終點的路線快取 30 分鐘，節省機車路線額度（免費每月 5,000 次）。

## 座標規則

- `GEOCODE`：HERE 自動計算，每天凌晨 4 點與後端啟動時清除（`GeoCleanupService`）。
- `MANUAL`：店員在地圖上「更新會員位置」，透過 `PUT /api/members/{phone}` 寫入，不會被每日清除。
- 會員地址被修改且座標沒變時，座標會被清空，下次用新地址重新計算。

## 錯誤

| 狀態 | 情況 |
|---|---|
| 400 | 會員沒有地址；找不到可行的機車路線 |
| 404 | 找不到會員；HERE 找不到這個地址 |
| 502 | HERE key 無效、額度用盡、連線失敗 |
| 503 | 後端尚未設定 `HERE_API_KEY` |
