// ─── MotorController.java ─────────────────────────────────────────────────────
package com.predictive.maintenance.controller;

import com.predictive.maintenance.dto.*;
import com.predictive.maintenance.service.MotorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/motors")
@RequiredArgsConstructor
public class MotorController {

    private final MotorService motorService;

    // POST /api/motors  ← register a new motor
    @PostMapping
    public ResponseEntity<MotorStatusResponse> create(
            @Valid @RequestBody MotorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(motorService.create(request));
    }

    // GET /api/motors  ← all motors with latest status
    @GetMapping
    public ResponseEntity<List<MotorStatusResponse>> getAll() {
        return ResponseEntity.ok(motorService.getAll());
    }

    // GET /api/status/{motorId}  ← matches your original API spec
    @GetMapping("/status/{motorId}")
    public ResponseEntity<MotorStatusResponse> getStatus(@PathVariable Long motorId) {
        return ResponseEntity.ok(motorService.getStatus(motorId));
    }
}

