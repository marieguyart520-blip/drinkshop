package com.example.drinkshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.time.Clock;
import java.time.ZoneId;

// 後端啟動入口；Spring 會掃描此 package 底下的 Controller、Service、Repository 與設定。
@SpringBootApplication
public class DrinkshopApplication {
    public static void main(String[] args) { SpringApplication.run(DrinkshopApplication.class, args); }
    // 統一使用台北時間判定營業日；測試可注入固定 Clock，不受測試當天日期影響。
    @Bean
    Clock clock() { return Clock.system(ZoneId.of("Asia/Taipei")); }
}
