// ─── AlertController.java ─────────────────────────────────────────────────────
package com.predictive.maintenance.controller;

import com.predictive.maintenance.dto.AlertResponse;
import com.predictive.maintenance.entity.Alert;
import com.predictive.maintenance.repository.AlertRepository;
import com.predictive.maintenance.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepository;
    private final AlertService    alertService;

    // GET /api/alerts  ← all unresolved alerts (for dashboard notification bell)
    @GetMapping
    public ResponseEntity<List<AlertResponse>> getActive() {
        return ResponseEntity.ok(
                alertRepository.findByResolvedFalseOrderByTimestampDesc()
                        .stream().map(this::toResponse).collect(Collectors.toList())
        );
    }

    // GET /api/alerts/{motorId}
    @GetMapping("/{motorId}")
    public ResponseEntity<List<AlertResponse>> getByMotor(@PathVariable Long motorId) {
        return ResponseEntity.ok(
                alertRepository.findByMotorIdOrderByTimestampDesc(motorId)
                        .stream().map(this::toResponse).collect(Collectors.toList())
        );
    }

    // PATCH /api/alerts/{alertId}/resolve
    @PatchMapping("/{alertId}/resolve")
    public ResponseEntity<AlertResponse> resolve(@PathVariable Long alertId) {
        Alert resolved = alertService.resolveAlert(alertId);
        return ResponseEntity.ok(toResponse(resolved));
    }

    private AlertResponse toResponse(Alert a) {
        return AlertResponse.builder()
                .id(a.getId())
                .motorId(a.getMotor().getId())
                .motorName(a.getMotor().getName())
                .type(a.getType())
                .severity(a.getSeverity())
                .message(a.getMessage())
                .resolved(a.getResolved())
                .timestamp(a.getTimestamp())
                .build();
    }
}

