package com.fisa.lep.inventory.repository;

import com.fisa.lep.area.entity.QArea;
import com.fisa.lep.inventory.entity.QInventory;
import com.fisa.lep.mart.entity.QMart;
import com.fisa.lep.product.entity.QProduct;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class InventoryRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public Optional<String[]> findLowestPrice(String depth1, String depth2, String depth3, String productName) {
        QArea qArea = QArea.area;
        QMart qMart = QMart.mart;
        QProduct qProduct = QProduct.product;
        QInventory qInventory = QInventory.inventory;

        List<String[]> results = queryFactory
                .select(qInventory.checkDate, qInventory.price, qProduct.name, qMart.name, qMart.brand, qArea.fullAddr)
                .from(qInventory)
                .join(qInventory.product, qProduct)
                .join(qInventory.mart, qMart)
                .join(qMart.area, qArea)
                .where(qArea.region1depthName.eq(depth1)
                        .and(qArea.region2depthName.eq(depth2))
                        .and(qArea.region3depthName.eq(depth3))
                        .and(qProduct.name.eq(productName)))
                .orderBy(qInventory.price.asc())
                .limit(3)
                .fetch()
                .stream()
                .map(tuple -> new String[]{
                        tuple.get(qInventory.checkDate).toString(),
                        tuple.get(qInventory.price).toString(),
                        tuple.get(qProduct.name),
                        tuple.get(qMart.name),
                        tuple.get(qMart.brand),
                        tuple.get(qArea.fullAddr)
                })
                .collect(Collectors.toList());

//        List<String[]> results = queryFactory
//                .select(Projections.fields(String[].class,
//                        qInventory.checkDate.as("checkDate"),
//                        qInventory.price.as("price"),
//                        qProduct.name.as("productName"),
//                        qMart.name.as("martName"),
//                        qMart.brand.as("martBrand"),
//                        qArea.fullAddr.as("fullAddr")))
//                .from(qInventory)
//                .join(qInventory.product, qProduct)
//                .join(qInventory.mart, qMart)
//                .join(qMart.area, qArea)
//                .where(qArea.region1depthName.eq(depth1)
//                        .and(qArea.region2depthName.eq(depth2))
//                        .and(qArea.region3depthName.eq(depth3))
//                        .and(qProduct.name.eq(productName)))
//                .orderBy(qInventory.price.asc())
//                .limit(3)
//                .fetch()
//                .stream()
//                .collect(Collectors.toList());
//
//
//        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));

//        List<String[]> results = queryFactory
//                .select(
//                        qInventory.checkDate,
//                        qInventory.price,
//                        qProduct.name,
//                        qMart.name,
//                        qMart.brand,
//                        qArea.fullAddr
//                )
//                .from(qInventory)
//                .join(qInventory.product, qProduct)
//                .join(qInventory.mart, qMart)
//                .join(qMart.area, qArea)
//                .where(
//                        qArea.region1depthName.eq(depth1)
//                        .and(qArea.region2depthName.eq(depth2))
//                        .and(qArea.region3depthName.eq(depth3))
//                        .and(qProduct.name.eq(productName))
//                )
//                .orderBy(qInventory.price.asc())
//                .limit(3)
//                .fetch()
//                .stream()
//                .map(tuple -> new String[]{
//                        tuple.get(0, String.class),     // qInventory.checkDate.toString()
//                        tuple.get(1, String.class),     // qInventory.price.toString()
//                        tuple.get(qProduct.name),
//                        tuple.get(qMart.name),
//                        tuple.get(qMart.brand),
//                        tuple.get(qArea.fullAddr)
//                })
//                .toList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
