package com.example.vpp;

import com.example.vpp.config.KafkaTestConfig;
import com.example.vpp.dto.Battery;
import com.example.vpp.dto.BatteryData;
import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.service.BatteryService;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = VppWorkerApplication.class)
@ExtendWith(SpringExtension.class)
public class BatteryServiceIntegrationTest {

    @MockBean
    private KafkaTemplate<String, BatteryData> kafkaTemplate;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine"
    );

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private BatteryService batteryService;

    @Test
    void testSaveAllAndGetBatteriesInRange() {
        // Save some batteries
        Battery b1 = new Battery("Battery A", 100, 2000.0);
        Battery b2 = new Battery("Battery B", 50, 2500.0);
        Battery b3 = new Battery("Battery C", 75, 2600.0);
        batteryService.saveAll(List.of(b1, b2, b3));

        // Query range including all
        BatteryStatsResponse response = batteryService.getBatteriesInRange(50, 100, Optional.empty(), Optional.empty());
        assertNotNull(response);
        assertEquals(3, response.getBatteries().size());
        assertEquals(7100.0, response.getTotalWattCapacity());
        assertEquals(2366.67, response.getAverageWattCapacity(), 0.01);

        // Query range with minWatt filter
        response = batteryService.getBatteriesInRange(60, 100, Optional.of(2000.0), Optional.empty());
        assertEquals(2, response.getBatteries().size());
        assertTrue(response.getBatteries().contains("Battery A"));
        assertTrue(response.getBatteries().contains("Battery C"));
    }
}
