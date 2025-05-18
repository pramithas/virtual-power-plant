package com.example.vpp.repository;

import com.example.vpp.model.BatteryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatteryRepository extends JpaRepository<BatteryEntity, Long> {
    List<BatteryEntity> findByPostcodeBetween(Integer start, Integer end);
}
