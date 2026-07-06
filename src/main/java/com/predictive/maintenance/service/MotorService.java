package com.predictive.maintenance.service;

import com.predictive.maintenance.dto.*;
import com.predictive.maintenance.entity.*;
import com.predictive.maintenance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MotorService {

    private final MotorRepository       motorRepository;
    private final SensorDataRepository  sensorDataRepository;
    private final AlertRepository       alertRepository;
    private final ThresholdRepository   thresholdRepository;

    // ─── Create a new motor ───────────────────────────────────────────────────
    @Transactional
    public MotorStatusResponse create(MotorRequest request) {
        Motor motor = Motor.builder()
                .name(request.getName())
                .location(request.getLocation())
                .status("NORMAL")
                .build();
        motor = motorRepository.save(motor);

        // Create default thresholds for this motor
        Threshold threshold = Threshold.builder().motor(motor).build();
        thresholdRepository.save(threshold);

        return toStatusResponse(motor);
    }

    // ─── Get all motors ───────────────────────────────────────────────────────
    public List<MotorStatusResponse> getAll() {
        return motorRepository.findAll()
                .stream()
                .map(this::toStatusResponse)
                .collect(Collectors.toList());
    }

    // ─── Get single motor status ──────────────────────────────────────────────
    public MotorStatusResponse getStatus(Long motorId) {
        Motor motor = motorRepository.findById(motorId)
                .orElseThrow(() -> new RuntimeException("Motor not found: " + motorId));
        return toStatusResponse(motor);
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────
    private MotorStatusResponse toStatusResponse(Motor motor) {
        var lastReading = sensorDataRepository
                .findTopByMotorIdOrderByTimestampDesc(motor.getId());

        long activeAlerts = alertRepository
                .countByMotorIdAndResolvedFalse(motor.getId());

        return MotorStatusResponse.builder()
                .id(motor.getId())
                .name(motor.getName())
                .location(motor.getLocation())
                .status(motor.getStatus())
                .lastTemperature(lastReading.map(SensorData::getTemperature).orElse(null))
                .lastVibration(lastReading.map(SensorData::getVibration).orElse(null))
                .lastCurrent(lastReading.map(SensorData::getCurrent).orElse(null))
                .lastReading(lastReading.map(SensorData::getTimestamp).orElse(null))
                .activeAlertCount(activeAlerts)
                .build();
    }
}