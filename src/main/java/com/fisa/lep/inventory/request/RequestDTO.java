package com.fisa.lep.inventory.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class RequestDTO {

    /**
     * 상품명: 머거본 알땅콩(135g)
     */
    private String productName;

    /**
     * 서울
     */
    private String region1depthName;

    /**
     * 중구
     */
    private String region2depthName;

    /**
     * 수표동
     */
    private String region3depthName;
}
