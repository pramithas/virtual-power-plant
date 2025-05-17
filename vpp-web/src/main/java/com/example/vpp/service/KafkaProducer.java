package com.example.vpp.service;

import com.example.vpp.dto.BatteryData;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, BatteryData> kafkaTemplate;
    private final String TOPIC = "vpp_topic";

    public KafkaProducer(KafkaTemplate<String, BatteryData> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(BatteryData message) {
        kafkaTemplate.send(TOPIC, message);
        System.out.println("Sent message: " + message);
    }
}
