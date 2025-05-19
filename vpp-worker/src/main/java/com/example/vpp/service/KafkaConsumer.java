package com.example.vpp.service;

import com.example.vpp.dto.Battery;
import com.example.vpp.dto.BatteryData;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer service for processing battery data messages.
 * Listens to the 'vpp_topic' topic and persists received battery data.
 */
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);
    private final BatteryService batteryService;

    /**
     * Consumes battery data messages from Kafka topic.
     * @param batteryData The received battery data payload
     * @throws RuntimeException if message processing fails
     */
    @KafkaListener(
            topics = "vpp_topic",
            groupId = "vpp",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(BatteryData batteryData) {
        try {
            LOG.info("Received Kafka message with {} batteries", batteryData.getBatteryList().size());

            if (batteryData.getBatteryList() == null || batteryData.getBatteryList().isEmpty()) {
                LOG.warn("Received empty battery list in Kafka message");
                return;
            }

            long startTime = System.currentTimeMillis();
            batteryService.saveAll(batteryData.getBatteryList());
            long processingTime = System.currentTimeMillis() - startTime;

            LOG.info("Successfully processed {} batteries in {} ms",
                    batteryData.getBatteryList().size(),
                    processingTime);

        } catch (Exception e) {
            LOG.error("Failed to process Kafka message. Battery list size: {}",
                    batteryData != null && batteryData.getBatteryList() != null
                            ? batteryData.getBatteryList().size()
                            : "null",
                    e);
            throw new RuntimeException("Kafka message processing failed", e);
        }
    }
}