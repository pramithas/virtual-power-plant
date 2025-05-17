package com.example.vpp.service;

import com.example.vpp.dto.Battery;
import com.example.vpp.dto.BatteryData;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final BatteryService batteryService;

    @KafkaListener(topics = "vpp_topic", groupId = "vpp", containerFactory = "kafkaListenerContainerFactory")
    public void consume(BatteryData batteryData) {
        System.out.println("Received message: " + batteryData.getBatteryList());
        batteryService.saveAll(batteryData.getBatteryList());
    }
}
