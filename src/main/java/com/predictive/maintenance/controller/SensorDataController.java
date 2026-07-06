// ─── SensorDataController.java ────────────────────────────────────────────────
package com.predictive.maintenance.controller;

import com.predictive.maintenance.dto.*;
import com.predictive.maintenance.service.SensorDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class SensorDataController {

    private final SensorDataService sensorDataService;

    // POST /api/data  ← ESP32 sends here every 5 seconds
    @PostMapping
    public ResponseEntity<SensorDataResponse> receive(
            @Valid @RequestBody SensorDataRequest request) {
        SensorDataResponse response = sensorDataService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/data/{motorId}?limit=50
    @GetMapping("/{motorId}")
    public ResponseEntity<List<SensorDataResponse>> getRecent(
            @PathVariable Long motorId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(sensorDataService.getRecent(motorId, limit));
    }

    // GET /api/data/{motorId}/range?from=...&to=...
    @GetMapping("/{motorId}/range")
    public ResponseEntity<List<SensorDataResponse>> getRange(
            @PathVariable Long motorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(sensorDataService.getRange(motorId, from, to));
    }
}





