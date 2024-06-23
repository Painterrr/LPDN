package com.fisa.lep.inventory.controller;

import com.fisa.lep.inventory.service.InventoryService;
import com.fisa.lep.inventory.request.RequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
@CrossOrigin
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * /api/lowest
     * @param requestDTO(region1depthName, region2depthName, region3depthName, productName)
     * @return String[]: { "martName, productName, price, area", "", "" }
     */
    @PostMapping("/lowest")
    public ResponseEntity<String[]> findLowestPrice(@RequestBody RequestDTO requestDTO) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String[] lowest = inventoryService.findLowestPrice(requestDTO);

        stopWatch.stop();
        log.info("** Time taken for findLowestPrice: {} ms", stopWatch.getTotalTimeMillis());

        if (lowest.length > 0) {
            log.info("** In MartController, lowest[0]: {}", lowest[0]);
        } else {
            log.warn("** No results found for request: {}", requestDTO);
        }

        return ResponseEntity.ok(lowest);
    }
}
