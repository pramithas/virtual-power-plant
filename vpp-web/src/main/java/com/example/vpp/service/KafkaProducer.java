package com.example.vpp.service;

import com.example.vpp.dto.BatteryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Service for producing battery data messages to Kafka.
 * Handles the publishing of battery information to the VPP topic.
 */
@Service
public class KafkaProducer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaProducer.class);
    private static final String TOPIC = "vpp_topic";

    private final KafkaTemplate<String, BatteryData> kafkaTemplate;

    /**
     * Constructs a KafkaProducer with the given KafkaTemplate.
     * @param kafkaTemplate The configured Kafka template for sending messages
     */
    public KafkaProducer(KafkaTemplate<String, BatteryData> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        LOG.info("KafkaProducer initialized for topic: {}", TOPIC);
    }

    /**
     * Sends a battery data message to the Kafka topic.
     * @param message The battery data payload to send
     * @throws RuntimeException if message sending fails
     */
    public void sendMessage(BatteryData message) {
        if (message == null) {
            LOG.error("Attempt to send null message to Kafka");
            throw new IllegalArgumentException("Message cannot be null");
        }

        LOG.debug("Message details: {}", message);

        try {
            long startTime = System.currentTimeMillis();
            CompletableFuture<SendResult<String, BatteryData>> future =
                    kafkaTemplate.send(TOPIC, message);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    LOG.error("Failed to send message to Kafka topic {}. Error: {}",
                            TOPIC, ex.getMessage(), ex);
                } else {
                    LOG.info("Successfully sent message to Kafka. Topic: {}, Partition: {}, Offset: {}, Time: {}ms",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            System.currentTimeMillis() - startTime);
                }
            });

            LOG.debug("Message dispatched to Kafka producer");
        } catch (Exception e) {
            LOG.error("Unexpected error while sending to Kafka topic {}. Error: {}",
                    TOPIC, e.getMessage(), e);
            throw new RuntimeException("Failed to send Kafka message", e);
        }
    }
}