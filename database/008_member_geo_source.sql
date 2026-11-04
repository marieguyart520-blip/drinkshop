-- 會員座標來源。GEOCODE = HERE 由地址自動計算（每日凌晨清除）；MANUAL = 店員手動設定（保留到地址被修改）。
-- 規則：有座標才有來源，沒有座標時 geo_source 必須是 NULL。
-- 在 Workbench 執行一次即可；不可重複執行。Safe Updates 會擋下 UPDATE，先執行 SET SQL_SAFE_UPDATES = 0;
ALTER TABLE member
  ADD geo_source ENUM('GEOCODE','MANUAL') NULL AFTER lng;
-- 既有座標都是透過 API 手動填入，標記為 MANUAL。
UPDATE member SET geo_source='MANUAL' WHERE lat IS NOT NULL;
ALTER TABLE member ADD CONSTRAINT chk_member_geo_source
  CHECK ((lat IS NULL AND geo_source IS NULL) OR (lat IS NOT NULL AND geo_source IS NOT NULL));
