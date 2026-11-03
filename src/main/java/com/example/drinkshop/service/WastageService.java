package com.example.drinkshop.service;

import com.example.drinkshop.dto.DailyWastageSummaryResponse;
import com.example.drinkshop.dto.OrderItemDto;
import com.example.drinkshop.entity.Inventory;
import com.example.drinkshop.entity.OrderItem;
import com.example.drinkshop.exception.ResourceNotFoundException;
import com.example.drinkshop.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 報廢損耗管理服務（處理原料報廢登記、單杯飲品作廢與當日損耗成本統計）
 */
@Service
@Transactional
public class WastageService {

    private final InventoryService inventoryService;
    private final OrderItemRepository orderItemRepository;

    public WastageService(InventoryService inventoryService, OrderItemRepository orderItemRepository) {
        this.inventoryService = inventoryService;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * 原料報廢登記（例如過期牛奶、煮壞茶湯、打翻茶葉等）
     * 連動扣除 Inventory 帳面庫存數量，並記錄通報人員與時間
     */
    public Inventory recordRawMaterialWastage(Long inventoryId, BigDecimal wastageQty, String reason, Long staffId) {
        if (wastageQty == null || wastageQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("報廢數量必須大於 0");
        }
        // 1. 自動扣除庫存（若庫存不足會拋出 InsufficientStockException）
        Inventory updatedInventory = inventoryService.deductStock(inventoryId, wastageQty);

        // 2. 記錄最後報廢通報人員與時間
        updatedInventory.setReportedQty(updatedInventory.getBookQty());
        updatedInventory.setReportedBy(staffId);
        updatedInventory.setReportedAt(LocalDateTime.now());

        return updatedInventory;
    }

    /**
     * 訂單飲品單品項報廢作廢（例如做錯甜度冰塊、客訴重做等）
     * 將 OrderItem 狀態更新為 WASTED，並記錄原因與審核主管員工編號
     */
    public OrderItem recordOrderItemWastage(Long itemId, String reason, Long staffId) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("作廢原因不能為空");
        }

        OrderItem orderItem = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("查無指定的訂單明細，ID: " + itemId));

        orderItem.setStatus("WASTED");
        orderItem.setReason(reason);
        orderItem.setApprovedBy(staffId);
        orderItem.setUpdatedAt(LocalDateTime.now());

        return orderItemRepository.save(orderItem);
    }

    /**
     * 每日報廢統計摘要：統計指定日期所有作廢飲品之總杯數與損失總成本
     */
    @Transactional(readOnly = true)
    public DailyWastageSummaryResponse getDailyWastageSummary(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<OrderItem> wastedItems = orderItemRepository.findByStatusAndCreatedAtBetween("WASTED", startOfDay, endOfDay);

        int totalCount = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        List<OrderItemDto> wastedItemDtos = new ArrayList<>();

        for (OrderItem item : wastedItems) {
            int qty = (item.getQty() != null ? item.getQty() : 1);
            totalCount += qty;

            if (item.getMaterialCost() != null) {
                totalCost = totalCost.add(item.getMaterialCost());
            } else if (item.getUnitPrice() != null) {
                // 若無精確原料成本，以飲品單價 * 數量作為估算損耗成本
                totalCost = totalCost.add(item.getUnitPrice().multiply(BigDecimal.valueOf(qty)));
            }
            wastedItemDtos.add(toDto(item));
        }

        return new DailyWastageSummaryResponse(date, totalCount, totalCost, wastedItemDtos);
    }

    /**
     * Entity 轉為扁平化 DTO
     */
    private OrderItemDto toDto(OrderItem item) {
        if (item == null) {
            return null;
        }
        return new OrderItemDto(
                item.getItemId(),
                item.getOrderId(),
                item.getProductId(),
                item.getProductName(),
                item.getSize(),
                item.getTemperature(),
                item.getSugar(),
                item.getIce(),
                item.getQty(),
                item.getUnitPrice(),
                item.getStatus(),
                item.getNote(),
                item.getCreatedAt()
        );
    }
}

