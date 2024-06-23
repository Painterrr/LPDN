package com.fisa.lep.area.repository;

import com.fisa.lep.area.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<Area, Long> {
    boolean existsByZoneNo(String zipCode);

    Area findByHjdCode(String hCode);

    Area findByZoneNo(String zoneNo);

    Area findByFullAddr(String fullAddr);

}
