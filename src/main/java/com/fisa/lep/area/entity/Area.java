package com.fisa.lep.area.entity;

import com.fisa.lep.area.dto.request.RequestAreaDTO;
import com.fisa.lep.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "area", indexes = {
        @Index(name = "idx_area_region", columnList = "region1depthName, region2depthName, region3depthName")
})
@Entity
public class Area extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Long id;

    // address_name
    private String fullAddr; // 서울 중구 수표동 99

    // 지역 1 Depth, 시도 단위
    private String region1depthName; //  서울

    // 지역 2 Depth, 구 단위
    private String region2depthName; // 중구

    // 지역 3 Depth, 동 단위
    private String region3depthName; // 수표동

    // 지역 3 Depth, 행정동 명칭
    private String region3depthHName; // 명동

    // 우편번호
    private String zoneNo; // 04542

    // 행정동 코드
    private String hjdCode; // 1114055000

    public static Area saveArea(RequestAreaDTO areaDTO) {
        Area area = new Area();
        area.fullAddr = areaDTO.getFullAddr();
        area.region1depthName = areaDTO.getRegion1depthName();
        area.region2depthName = areaDTO.getRegion2depthName();
        area.region3depthName = areaDTO.getRegion3depthName();
        area.region3depthHName = areaDTO.getRegion3depthHName();
        area.zoneNo = areaDTO.getZoneNo();
        area.hjdCode = areaDTO.getHjdCode();

        return area;
    }

}