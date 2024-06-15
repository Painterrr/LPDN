package com.fisa.lep.mart.dto.request;

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
     * 행정동: 서울특별시 마포구 합정동
     */
    private String fullAddr;
}
