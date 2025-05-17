package com.example.vpp.service;

import com.example.vpp.dto.BatteryData;
import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.dto.Battery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BatteryService {

    private final KafkaProducer kafkaProducer;

    public void saveAll(List<Battery> batteries) {
        BatteryData batteryData = BatteryData.builder().batteryList(batteries).build();
        kafkaProducer.sendMessage(batteryData);
    }

    public void deleteAll() {
//        repository.deleteAll();
    }

    public BatteryStatsResponse getBatteriesInRange(int start, int end, Optional<Double> minWatt, Optional<Double> maxWatt) {
        return null;
    }
}
