package com.example.drinkshop.service;

import com.example.drinkshop.repository.PosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** 每日清除 HERE 自動計算的會員座標（GEOCODE）；店員手動設定的（MANUAL）保留。 */
@Service
public class GeoCleanupService {
    private static final Logger log = LoggerFactory.getLogger(GeoCleanupService.class);
    private final PosRepository repo;

    public GeoCleanupService(PosRepository repo) { this.repo = repo; }

    /** 每天凌晨 4 點（台北時間）執行。 */
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Taipei")
    public void daily() { clear("每日排程"); }

    /** 後端啟動時也清一次：關店時後端如果是關機的，凌晨 4 點的排程不會執行。 */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() { clear("啟動時"); }

    private void clear(String reason) {
        try {
            int count = repo.update(
                "UPDATE member SET lat=NULL, lng=NULL, geo_source=NULL WHERE geo_source='GEOCODE'");
            log.info("{}清除會員自動座標 {} 筆", reason, count);
        } catch (RuntimeException ex) {
            // 清除失敗不影響營業，下次排程或重啟時會再試
            log.warn("{}清除會員座標失敗：{}", reason, ex.getClass().getSimpleName());
        }
    }
}