package com.fisa.lep.inventory.service;

import com.fisa.lep.inventory.repository.InventoryRepository;
import com.fisa.lep.inventory.repository.InventoryRepositoryCustom;
import com.fisa.lep.inventory.request.RequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepositoryCustom inventoryRepositoryCustom;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepositorym, InventoryRepositoryCustom inventoryRepositoryCustom) {
        this.inventoryRepository = inventoryRepositorym;
        this.inventoryRepositoryCustom = inventoryRepositoryCustom;
    }

    @Transactional(readOnly = true)
    public String[] findLowestPrice(RequestDTO requestDTO) {
        String productName = requestDTO.getProductName();
        String depth1 = requestDTO.getRegion1depthName();
        String depth2 = requestDTO.getRegion2depthName();
        String depth3 = requestDTO.getRegion3depthName();

        Optional<String[]> response = inventoryRepositoryCustom.findLowestPrice(depth1, depth2, depth3, productName);
//        Optional<String[]> response = inventoryRepository.findLowestPriceNative(depth1, depth2, depth3, productName);

        return response.orElseGet(() -> new String[]{"해당 상품을 보유한 매점이 지역에 없습니다."});
    }
}
