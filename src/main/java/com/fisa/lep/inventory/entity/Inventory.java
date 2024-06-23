package com.fisa.lep.inventory.entity;

import com.fisa.lep.area.dto.request.RequestAreaDTO;
import com.fisa.lep.common.BaseEntity;
import com.fisa.lep.mart.entity.Mart;
import com.fisa.lep.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "inventory", indexes = {
        @Index(name = "idx_inventory_price", columnList = "price"),
        @Index(name = "idx_inventory_date_product", columnList = "check_date, product_id")
})
@Entity
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    private LocalDate checkDate; // 조사일

    private BigDecimal price; // 판매가격

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mart_id")
    private Mart mart; // 판매업소


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 상품

    @Builder
    public Inventory(LocalDate checkDate, BigDecimal price, Mart mart, Product product) {
        this.checkDate = checkDate;
        this.price = price;
        this.mart = mart;
        this.product = product;
    }

}