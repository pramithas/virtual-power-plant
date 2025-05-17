package com.example.vpp.controller;

import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.dto.Battery;
import com.example.vpp.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/batteries")
@RequiredArgsConstructor
public class BatteryController {

    private final BatteryService service;

    @PostMapping
    public ResponseEntity<Void> addBatteries(@RequestBody List<Battery> batteries) {
        service.saveAll(batteries);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllBatteries() {
//        service.deleteAll();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/range")
    public ResponseEntity<BatteryStatsResponse> getBatteriesInRange(
            @RequestParam int start,
            @RequestParam int end,
            @RequestParam Optional<Double> minWatt,
            @RequestParam Optional<Double> maxWatt) {
        return ResponseEntity.ok(service.getBatteriesInRangeReactive(start, end, minWatt, maxWatt));
    }
}
