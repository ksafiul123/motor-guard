package com.predictive.maintenance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ─── Motor status response ────────────────────────────────────────────────────
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorStatusResponse {
    private Long id;
    private String name;
    private String location;
    private String status;
    private BigDecimal lastTemperature;
    private BigDecimal lastVibration;
    private BigDecimal lastCurrent;
    private LocalDateTime lastReading;
    private long activeAlertCount;
}
