package com.example.vpp;

import com.example.vpp.dto.Battery;
import com.example.vpp.dto.BatteryData;
import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.service.BatteryService;
import com.example.vpp.service.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BatteryServiceTest {

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private BatteryService batteryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveAll() {
        List<Battery> batteryList = List.of(new Battery("Battery1", 100, 20.0));
        batteryService.saveAll(batteryList);

        ArgumentCaptor<BatteryData> captor = ArgumentCaptor.forClass(BatteryData.class);
        verify(kafkaProducer, times(1)).sendMessage(captor.capture());

        BatteryData captured = captor.getValue();
        assertEquals(1, captured.getBatteryList().size());
        assertEquals("Battery1", captured.getBatteryList().get(0).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetBatteriesInRangeReactive() {
        BatteryStatsResponse mockResponse = new BatteryStatsResponse(List.of("Battery1", "Battery2"), 300.0, 100.0);

        // Force type casting for generics to satisfy Mockito
        when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(BatteryStatsResponse.class)).thenReturn(Mono.just(mockResponse));

        BatteryStatsResponse result = batteryService.getBatteriesInRangeReactive(1, 2, Optional.empty(), Optional.empty());

        assertNotNull(result);
        assertEquals(2, result.getBatteries().size());
        assertEquals(300.0, result.getTotalWattCapacity());
    }
}
