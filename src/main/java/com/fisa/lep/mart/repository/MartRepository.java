package com.fisa.lep.mart.repository;

import com.fisa.lep.area.entity.Area;
import com.fisa.lep.mart.entity.Mart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MartRepository extends JpaRepository<Mart, Long> {

    

    List<Mart> findByArea(Area area);

    Optional<Mart> findByName(String name);

    boolean existsByName(String name);

    Mart save(Mart mart);
}
