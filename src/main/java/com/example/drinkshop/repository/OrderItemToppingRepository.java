package com.example.drinkshop.repository;

import com.example.drinkshop.entity.OrderItemTopping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 訂單品項加料關聯資料存取層
 */
@Repository
public interface OrderItemToppingRepository extends JpaRepository<OrderItemTopping, Long> {

    // 依品項明細編號查詢該杯飲料的所有加料
    List<OrderItemTopping> findByItemId(Long itemId);

    // 依加料項目編號查詢使用紀錄
    List<OrderItemTopping> findByToppingId(Long toppingId);

    // 依品項與特定加料查詢單筆關聯紀錄
    Optional<OrderItemTopping> findByItemIdAndToppingId(Long itemId, Long toppingId);
}
