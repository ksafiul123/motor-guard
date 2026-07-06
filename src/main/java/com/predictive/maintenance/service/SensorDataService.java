package com.predictive.maintenance.service;

import com.predictive.maintenance.dto.*;
import com.predictive.maintenance.entity.*;
import com.predictive.maintenance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorDataService {

    private final SensorDataRepository sensorDataRepository;
    private final MotorRepository      motorRepository;
    private final ThresholdRepository  thresholdRepository;
    private final AlertService         alertService;

    // ─── Receive data from ESP32 ──────────────────────────────────────────────
    @Transactional
    public SensorDataResponse save(SensorDataRequest request) {
        // Find motor
        Motor motor = motorRepository.findById(request.getMotorId())
                .orElseThrow(() -> new RuntimeException(
                        "Motor not found with id: " + request.getMotorId()));

        // Get thresholds for this motor
        Threshold threshold = thresholdRepository.findByMotorId(motor.getId())
                .orElse(Threshold.builder().motor(motor).build());

        // Build SensorData entity
        SensorData data = SensorData.builder()
                .motor(motor)
                .temperature(request.getTemperature())
                .vibration(request.getVibration())
                .current(request.getCurrent())
                .timestamp(LocalDateTime.now())
                .build();

        // Evaluate and set status
        String status = alertService.determineStatus(data, threshold);
        data.setStatus(status);

        // Save reading
        SensorData saved = sensorDataRepository.save(data);
        log.info("[DATA] Motor '{}' → Temp:{} Vib:{} Curr:{} Status:{}",
                motor.getName(),
                request.getTemperature(),
                request.getVibration(),
                request.getCurrent(),
                status);

        // Update motor status
        motor.setStatus(status);
        motorRepository.save(motor);

        // Run alert evaluation
        alertService.evaluate(motor, saved);

        return toResponse(saved);
    }

    // ─── Get last N readings for a motor ──────────────────────────────────────
    public List<SensorDataResponse> getRecent(Long motorId, int limit) {
        return sensorDataRepository
                .findByMotorIdOrderByTimestampDesc(motorId, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Get all readings for a motor ─────────────────────────────────────────
    public List<SensorDataResponse> getAll(Long motorId) {
        return sensorDataRepository
                .findByMotorIdOrderByTimestampDesc(motorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Get readings in a time range ─────────────────────────────────────────
    public List<SensorDataResponse> getRange(Long motorId,
                                             LocalDateTime from,
                                             LocalDateTime to) {
        return sensorDataRepository
                .findByMotorIdAndTimestampBetweenOrderByTimestampAsc(motorId, from, to)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────
    private SensorDataResponse toResponse(SensorData s) {
        return SensorDataResponse.builder()
                .id(s.getId())
                .motorId(s.getMotor().getId())
                .temperature(s.getTemperature())
                .vibration(s.getVibration())
                .current(s.getCurrent())
                .status(s.getStatus())
                .timestamp(s.getTimestamp())
                .build();
    }
}