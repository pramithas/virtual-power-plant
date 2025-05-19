package com.example.vpp.service;

import com.example.vpp.dto.BatteryData;
import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.dto.Battery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for battery operations including:
 * - Publishing battery data to Kafka
 * - Retrieving battery statistics via reactive web client
 */
@Service
@RequiredArgsConstructor
public class BatteryService {

    private static final Logger LOG = LoggerFactory.getLogger(BatteryService.class);
    private final KafkaProducer kafkaProducer;
    private final WebClient webClient;

    /**
     * Saves a list of batteries by publishing them to Kafka
     * @param batteries List of Battery objects to be saved
     * @throws RuntimeException if Kafka message publishing fails
     */
    public void saveAll(List<Battery> batteries) {
        LOG.info("Preparing to save {} batteries via Kafka", batteries.size());

        if (batteries == null || batteries.isEmpty()) {
            LOG.warn("Attempted to save empty/null battery list");
            throw new IllegalArgumentException("Batteries list cannot be null or empty");
        }

        try {
            BatteryData batteryData = BatteryData.builder()
                    .batteryList(batteries)
                    .build();

            LOG.debug("Constructed BatteryData with {} entries", batteries.size());
            long startTime = System.currentTimeMillis();

            kafkaProducer.sendMessage(batteryData);

            LOG.info("Successfully published {} batteries to Kafka in {} ms",
                    batteries.size(),
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LOG.error("Failed to publish batteries to Kafka. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Kafka publishing failed", e);
        }
    }

    /**
     * Retrieves battery statistics reactively within specified range and filters
     *
     * @param start Starting postcode (inclusive)
     * @param end Ending postcode (inclusive)
     * @param minWatt Optional minimum wattage filter
     * @param maxWatt Optional maximum wattage filter
     * @return BatteryStatsResponse containing filtered results
     * @throws RuntimeException if web client call fails
     */
    public BatteryStatsResponse getBatteriesInRangeReactive(
            int start,
            int end,
            Optional<Double> minWatt,
            Optional<Double> maxWatt) {

        LOG.info("Initiating reactive request for batteries in range {}-{} with wattage filters [{}, {}]",
                start, end, minWatt.orElse(null), maxWatt.orElse(null));

        if (start > end) {
            LOG.error("Invalid range: start {} > end {}", start, end);
            throw new IllegalArgumentException("Start must be <= end");
        }

        long startTime = System.currentTimeMillis();
        try {
            BatteryStatsResponse response = webClient.get()
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
                            res -> {
                                LOG.error("Received error status: {}", res.statusCode());
                                return res.bodyToMono(String.class)
                                        .flatMap(body -> {
                                            LOG.error("Error response body: {}", body);
                                            return Mono.error(new RuntimeException(
                                                    "API Error: " + res.statusCode() + " - " + body));
                                        });
                            }
                    )
                    .bodyToMono(BatteryStatsResponse.class)
                    .doOnSuccess(r -> LOG.debug("Received successful response with {} batteries",
                            r.getBatteryNames().size()))
                    .block();

            LOG.info("Retrieved {} batteries in range in {} ms",
                    Objects.requireNonNull(response).getBatteryNames().size(),
                    System.currentTimeMillis() - startTime);
            return response;
        } catch (Exception e) {
            LOG.error("Failed to retrieve batteries. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Battery retrieval failed", e);
        }
    }
}