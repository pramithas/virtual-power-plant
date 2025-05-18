package com.example.vpp;

import com.example.vpp.dto.Battery;
import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.dto.BatteryData;
import com.example.vpp.service.BatteryService;
import com.example.vpp.service.KafkaProducer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class BatteryServiceIntegrationTest {

    @MockBean
    private KafkaProducer kafkaProducer;

    private MockWebServer mockWebServer;

    @Autowired
    private BatteryService batteryService;

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8081);  // Start server on port 8081

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        setWebClient(batteryService, webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testSaveAll_sendsKafkaMessage() {
        List<Battery> batteries = List.of(new Battery("Battery1", 100, 20.0));

        batteryService.saveAll(batteries);

        ArgumentCaptor<BatteryData> captor = ArgumentCaptor.forClass(BatteryData.class);
        verify(kafkaProducer, times(1)).sendMessage(captor.capture());

        BatteryData sentData = captor.getValue();
        assertEquals(1, sentData.getBatteryList().size());
        assertEquals("Battery1", sentData.getBatteryList().get(0).getName());
    }

    @Test
    void testGetBatteriesInRangeReactive() {
        // Prepare mock response JSON (adjust according to your BatteryStatsResponse JSON structure)
        String jsonResponse = "{\"batteries\":[\"Battery1\",\"Battery2\"],\"totalWattCapacity\":300.0,\"minWatt\":100.0}";

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        BatteryStatsResponse response = batteryService.getBatteriesInRangeReactive(1, 2, Optional.empty(), Optional.empty());

        assertNotNull(response);
        assertEquals(2, response.getBatteries().size());
        assertEquals(300.0, response.getTotalWattCapacity());
    }

    private void setWebClient(BatteryService service, WebClient client) {
        try {
            java.lang.reflect.Field field = BatteryService.class.getDeclaredField("webClient");
            field.setAccessible(true);
            field.set(service, client);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
