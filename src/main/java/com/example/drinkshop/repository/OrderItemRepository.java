package com.example.drinkshop.repository;

import com.example.drinkshop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單明細品項資料存取層
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 依訂單編號查詢所有品項明細
    List<OrderItem> findByOrderId(Long orderId);

    // 依商品品項編號查詢
    List<OrderItem> findByProductId(Long productId);

    // 依品項製作狀態查詢（例如 PENDING、MAKING、DONE、WASTED 等）
    List<OrderItem> findByStatus(String status);

    // 依訂單編號與品項狀態查詢
    List<OrderItem> findByOrderIdAndStatus(Long orderId, String status);

    // 依審核/報廢主管人員查詢
    List<OrderItem> findByApprovedBy(Long approvedBy);

    // 依建立時間區間查詢品項明細
    List<OrderItem> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 依狀態與建立時間區間查詢（常用於統計特定日期的報廢品項 WASTED）
    List<OrderItem> findByStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
}
