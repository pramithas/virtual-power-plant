package com.example.vpp.controller;

import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

/**
 * REST controller for managing battery-related operations.
 * Provides endpoints for retrieving battery statistics within specified ranges.
 */
@RestController
@RequestMapping("/api/batteries")
@RequiredArgsConstructor
public class BatteryController {

    private static final Logger LOG = LoggerFactory.getLogger(BatteryController.class);
    private final BatteryService service;

    /**
     * Retrieves battery statistics within the specified postcode range and optional wattage filters.
     *
     * @param start The starting postcode of the range (inclusive)
     * @param end The ending postcode of the range (inclusive)
     * @param minWatt Optional minimum watt capacity filter (inclusive)
     * @param maxWatt Optional maximum watt capacity filter (inclusive)
     * @return ResponseEntity containing battery statistics
     * @throws IllegalArgumentException if start postcode is greater than end postcode
     * @apiNote Example: GET /api/batteries/range?start=1000&end=2000&minWatt=100.0
     */
    @GetMapping("/range")
    public ResponseEntity<BatteryStatsResponse> getBatteriesInRange(
            @RequestParam int start,
            @RequestParam int end,
            @RequestParam Optional<Double> minWatt,
            @RequestParam Optional<Double> maxWatt) {

        LOG.info("Received battery range request - start: {}, end: {}, minWatt: {}, maxWatt: {}",
                start, end, minWatt.orElse(null), maxWatt.orElse(null));

        try {
            long startTime = System.currentTimeMillis();
            BatteryStatsResponse response = service.getBatteriesInRange(start, end, minWatt, maxWatt);

            LOG.debug("Battery stats calculated - total: {}, avg: {}, names: {}",
                    response.getTotalWattCapacity(),
                    response.getAverageWattCapacity(),
                    response.getBatteryNames().size());

            LOG.info("Request processed in {} ms", System.currentTimeMillis() - startTime);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            LOG.error("Invalid range parameters - start: {}, end: {}", start, end, e);
            throw e;
        } catch (Exception e) {
            LOG.error("Unexpected error processing battery range request", e);
            throw e;
        }
    }
}