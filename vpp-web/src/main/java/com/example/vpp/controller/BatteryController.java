package com.example.vpp.controller;

import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.dto.Battery;
import com.example.vpp.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing battery operations.
 * Provides endpoints for adding batteries and querying battery statistics.
 */
@RestController
@RequestMapping("/api/batteries")
@RequiredArgsConstructor
public class BatteryController {

    private static final Logger LOG = LoggerFactory.getLogger(BatteryController.class);
    private final BatteryService service;

    /**
     * Adds multiple batteries to the system.
     *
     * @param batteries List of battery objects to be added
     * @return ResponseEntity with HTTP 201 (Created) status
     * @throws IllegalArgumentException if batteries list is empty
     */
    @PostMapping
    public ResponseEntity<Void> addBatteries(@RequestBody List<Battery> batteries) {
        LOG.info("Received request to add {} batteries", batteries.size());

        if (batteries.isEmpty()) {
            LOG.warn("Attempt to add empty/null battery list");
            throw new IllegalArgumentException("Batteries list cannot be empty");
        }

        long startTime = System.currentTimeMillis();
        try {
            service.saveAll(batteries);
            LOG.info("Successfully added {} batteries in {} ms",
                    batteries.size(),
                    System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            LOG.error("Failed to add batteries. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves battery statistics within a specified postcode range and optional wattage filters.
     *
     * @param start Starting postcode (inclusive)
     * @param end Ending postcode (inclusive)
     * @param minWatt Optional minimum wattage filter
     * @param maxWatt Optional maximum wattage filter
     * @return ResponseEntity containing battery statistics
     * @throws IllegalArgumentException if start > end
     */
    @GetMapping("/range")
    public ResponseEntity<BatteryStatsResponse> getBatteriesInRange(
            @RequestParam int start,
            @RequestParam int end,
            @RequestParam Optional<Double> minWatt,
            @RequestParam Optional<Double> maxWatt) {

        LOG.info("Received range query - start: {}, end: {}, minWatt: {}, maxWatt: {}",
                start, end, minWatt.orElse(null), maxWatt.orElse(null));

        if (start > end) {
            LOG.error("Invalid range parameters: start {} > end {}", start, end);
            throw new IllegalArgumentException("Start postcode must be less than or equal to end postcode");
        }

        long startTime = System.currentTimeMillis();
        try {
            BatteryStatsResponse response = service.getBatteriesInRangeReactive(start, end, minWatt, maxWatt);
            LOG.info("Returning {} batteries in range. Processing time: {} ms",
                    response.getBatteryNames().size(),
                    System.currentTimeMillis() - startTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOG.error("Error processing range query: {}", e.getMessage(), e);
            throw e;
        }
    }
}