package com.example.vpp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatteryStatsResponse implements Serializable {
    private List<String> batteryNames;
    private double totalWattCapacity;
    private double averageWattCapacity;
}
