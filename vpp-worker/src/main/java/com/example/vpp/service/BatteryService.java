package com.example.vpp.service;

import com.example.vpp.dto.Battery;
import com.example.vpp.dto.BatteryStatsResponse;
import com.example.vpp.model.BatteryEntity;
import com.example.vpp.repository.BatteryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;

@Service
@RequiredArgsConstructor
public class BatteryService {

    private final BatteryRepository batteryRepository;

    public void saveAll(List<Battery> batteries) {
        List<BatteryEntity> entities = batteries.stream()
                .map(battery -> {
                    BatteryEntity entity = new BatteryEntity();
                    entity.setName(battery.getName());
                    entity.setCapacity(battery.getCapacity());
                    entity.setPostcode(battery.getPostcode());
                    return entity;
                })
                .toList();

        batteryRepository.saveAll(entities);
    }


//    public void deleteAll() {
//            repository.deleteAll();
//    }

    public BatteryStatsResponse getBatteriesInRange(int start, int end, Optional<Double> minWatt, Optional<Double> maxWatt) {
        return batteryRepository.findByPostcodeBetween(start, end).stream()
                .filter(b -> minWatt.map(min -> b.getCapacity() >= min).orElse(true))
                .filter(b -> maxWatt.map(max -> b.getCapacity() <= max).orElse(true))
                .sorted(Comparator.comparing(BatteryEntity::getName))
                .collect(collectingAndThen(
                        Collectors.toList(),
                        batteries -> {
                            List<String> names = batteries.stream().map(BatteryEntity::getName).collect(Collectors.toList());
                            double total = batteries.stream().mapToDouble(BatteryEntity::getCapacity).sum();
                            double avg = batteries.isEmpty() ? 0 : total / batteries.size();
                            return new BatteryStatsResponse(names, total, avg);
                        }
                ));
    }
}
