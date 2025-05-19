package com.example.vpp;

import com.example.vpp.dto.Battery;
import com.example.vpp.dto.BatteryData;
import com.example.vpp.service.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaProducerTest {

    @Mock
    private KafkaTemplate<String, BatteryData> kafkaTemplate;

    @InjectMocks
    private KafkaProducer kafkaProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendMessage_successfulSend() {
        // Arrange
        Battery data = new Battery();
        data.setName("battery-001");
        data.setCapacity(12.5);
        data.setPostcode(30);

        BatteryData batteryData = new BatteryData();
        batteryData.setBatteryList(List.of(data));

        SendResult<String, BatteryData> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, BatteryData>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(anyString(), any(BatteryData.class))).thenReturn(future);

        // Act & Assert
        assertDoesNotThrow(() -> kafkaProducer.sendMessage(batteryData));
        verify(kafkaTemplate, times(1)).send("vpp_topic", batteryData);
    }

    @Test
    void testSendMessage_nullMessage_throwsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                kafkaProducer.sendMessage(null)
        );
        assertEquals("Message cannot be null", exception.getMessage());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }

    @Test
    void testSendMessage_kafkaThrowsException_logsAndRethrows() {
        // Arrange
        Battery data = new Battery();
        data.setName("battery-002");
        data.setCapacity(10.5);
        data.setPostcode(25);

        BatteryData batteryData = new BatteryData();
        batteryData.setBatteryList(List.of(data));

        when(kafkaTemplate.send(anyString(), any(BatteryData.class)))
                .thenThrow(new RuntimeException("Kafka send failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                kafkaProducer.sendMessage(batteryData)
        );
        assertTrue(exception.getMessage().contains("Failed to send Kafka message"));
        verify(kafkaTemplate, times(1)).send("vpp_topic", batteryData);
    }
}

