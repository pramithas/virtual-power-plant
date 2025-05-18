package com.example.vpp.controller;

import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/batteries")
@RequiredArgsConstructor
public class BatteryController {

    private final BatteryService service;

    @GetMapping("/range")
    public ResponseEntity<BatteryStatsResponse> getBatteriesInRange(
            @RequestParam int start,
            @RequestParam int end,
            @RequestParam Optional<Double> minWatt,
            @RequestParam Optional<Double> maxWatt) {
        return ResponseEntity.ok(service.getBatteriesInRange(start, end, minWatt, maxWatt));
    }
}
