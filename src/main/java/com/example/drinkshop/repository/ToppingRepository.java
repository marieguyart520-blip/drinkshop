package com.example.drinkshop.repository;

import com.example.drinkshop.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 加料配料設定資料存取層
 */
@Repository
public interface ToppingRepository extends JpaRepository<Topping, Long> {

    // 依配料名稱查詢
    Optional<Topping> findByName(String name);

    // 依對應的原料庫存編號查詢加料項目
    List<Topping> findByInventoryId(Long inventoryId);

    // 依在售狀態查詢加料項目（例如查詢目前販售中的加料清單）
    List<Topping> findByOnSale(Boolean onSale);
}
