package com.example.vpp;

import com.example.vpp.dto.BatteryData;
import com.example.vpp.service.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class KafkaProducerTest {

    private KafkaTemplate<String, BatteryData> kafkaTemplate;
    private KafkaProducer kafkaProducer;

    @BeforeEach
    void setup() {
        kafkaTemplate = mock(KafkaTemplate.class);
        kafkaProducer = new KafkaProducer(kafkaTemplate);
    }

    @Test
    void testSendMessage() {
        BatteryData testData = new BatteryData(); // Populate if needed
        kafkaProducer.sendMessage(testData);

        verify(kafkaTemplate, times(1)).send(eq("vpp_topic"), eq(testData));
    }
}
