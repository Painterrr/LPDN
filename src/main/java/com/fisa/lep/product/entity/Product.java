package com.fisa.lep.product.entity;

import com.fisa.lep.area.entity.Area;
import com.fisa.lep.common.BaseEntity;
import com.fisa.lep.mart.entity.Mart;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "product", indexes = {
        @Index(name = "idx_product_name", columnList = "name")
})
@Entity
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    private String name; // 상품명

    private String company; // 제조사

    @Builder
    public Product(String name, String company) {
        this.name = name;
        this.company = company;
    }

}