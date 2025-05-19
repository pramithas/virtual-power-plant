package com.example.vpp.service;

import com.example.vpp.dto.Battery;
import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.model.BatteryEntity;
import com.example.vpp.repository.BatteryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;

/**
 * Service layer for battery-related operations including storage and statistics calculation.
 */
@Service
@RequiredArgsConstructor
public class BatteryService {

    private static final Logger LOG = LoggerFactory.getLogger(BatteryService.class);
    private final BatteryRepository batteryRepository;

    /**
     * Saves a list of batteries to the database.
     * @param batteries List of Battery DTOs to be saved
     */
    public void saveAll(List<Battery> batteries) {
        LOG.info("Saving {} batteries to repository", batteries.size());

        List<BatteryEntity> entities = batteries.stream()
                .map(battery -> {
                    BatteryEntity entity = new BatteryEntity();
                    entity.setName(battery.getName());
                    entity.setCapacity(battery.getCapacity());
                    entity.setPostcode(battery.getPostcode());
                    return entity;
                })
                .toList();

        long startTime = System.currentTimeMillis();
        List<BatteryEntity> savedEntities = batteryRepository.saveAll(entities);
        LOG.debug("Saved {} batteries in {} ms", savedEntities.size(), System.currentTimeMillis() - startTime);
    }

    /**
     * Retrieves battery statistics within a postcode range with optional wattage filters.
     *
     * @param start Starting postcode (inclusive)
     * @param end Ending postcode (inclusive)
     * @param minWatt Optional minimum watt capacity filter
     * @param maxWatt Optional maximum watt capacity filter
     * @return BatteryStatsResponse containing filtered results
     * @throws IllegalArgumentException if start > end
     */
    public BatteryStatsResponse getBatteriesInRange(int start, int end, Optional<Double> minWatt, Optional<Double> maxWatt) {
        LOG.info("Fetching batteries in postcode range [{}, {}] with wattage filters [min: {}, max: {}]",
                start, end, minWatt.orElse(null), maxWatt.orElse(null));

        // Validate range
        if (start > end) {
            LOG.error("Invalid postcode range: start {} > end {}", start, end);
            throw new IllegalArgumentException("Start postcode must be <= end postcode");
        }

        long queryStart = System.currentTimeMillis();
        List<BatteryEntity> batteriesInRange = batteryRepository.findByPostcodeBetween(start, end);
        LOG.debug("Found {} batteries in range in {} ms", batteriesInRange.size(), System.currentTimeMillis() - queryStart);

        // Process filtering and calculations
        long processStart = System.currentTimeMillis();
        BatteryStatsResponse response = batteriesInRange.stream()
                .filter(b -> minWatt.map(min -> b.getCapacity() >= min).orElse(true))
                .filter(b -> maxWatt.map(max -> b.getCapacity() <= max).orElse(true))
                .sorted(Comparator.comparing(BatteryEntity::getName))
                .collect(collectingAndThen(
                        Collectors.toList(),
                        filteredBatteries -> {
                            LOG.debug("After filtering: {} batteries meet criteria", filteredBatteries.size());

                            List<String> names = filteredBatteries.stream()
                                    .map(BatteryEntity::getName)
                                    .collect(Collectors.toList());

                            double total = filteredBatteries.stream()
                                    .mapToDouble(BatteryEntity::getCapacity)
                                    .sum();

                            double avg = filteredBatteries.isEmpty() ? 0 : total / filteredBatteries.size();

                            LOG.debug("Calculated stats - Total: {}, Avg: {}, Names: {}",
                                    total, avg, names.size());

                            return new BatteryStatsResponse(names, total, avg);
                        }
                ));

        LOG.info("Processed range request in {} ms ({} final batteries)",
                System.currentTimeMillis() - processStart,
                response.getBatteryNames().size());

        return response;
    }
}