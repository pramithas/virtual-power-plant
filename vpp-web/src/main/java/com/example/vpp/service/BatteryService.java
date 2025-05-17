package com.example.vpp.service;

import com.example.vpp.dto.BatteryData;
import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.dto.Battery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BatteryService {

    private final KafkaProducer kafkaProducer;
    private final WebClient webClient;

    public void saveAll(List<Battery> batteries) {
        BatteryData batteryData = BatteryData.builder().batteryList(batteries).build();
        kafkaProducer.sendMessage(batteryData);
    }

    public BatteryStatsResponse getBatteriesInRangeReactive(int start, int end,
                                                                  Optional<Double> minWatt, Optional<Double> maxWatt) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/batteries/range")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .queryParamIfPresent("minWatt", minWatt)
                        .queryParamIfPresent("maxWatt", maxWatt)
                        .build())
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException(
                                        "Failed to get batteries: " + response.statusCode() + " - " + body))
                )
                .bodyToMono(BatteryStatsResponse.class)
                .block();
    }
}
