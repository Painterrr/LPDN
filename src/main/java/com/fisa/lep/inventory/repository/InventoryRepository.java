package com.fisa.lep.inventory.repository;

import com.fisa.lep.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query(value = "SELECT i.check_date, i.price, p.name AS product_name, m.name AS mart_name, m.brand AS mart_brand, a.full_addr " +
            "FROM inventory i USE INDEX (idx_inventory_date_product) " +
            "JOIN product p USE INDEX (idx_product_name) ON i.product_id = p.product_id " +
            "JOIN mart m USE INDEX (idx_mart_name_brand) ON i.mart_id = m.mart_id " +
            "JOIN area a USE INDEX (idx_area_region_depth) ON m.area_id = a.area_id " +
            "WHERE a.region1dept_name = :depth1 " +
            "  AND a.region2depth_name = :depth2 " +
            "  AND a.region3depth_name = :depth3 " +
            "  AND p.name = :productName " +
            "ORDER BY i.price ASC " +
            "LIMIT 3", nativeQuery = true)
    Optional<String[]> findLowestPriceNative(
            @Param("depth1") String depth1,
            @Param("depth2") String depth2,
            @Param("depth3") String depth3,
            @Param("productName") String productName);

}
