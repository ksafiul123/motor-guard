package com.predictive.maintenance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ─── Sensor data response to frontend ────────────────────────────────────────
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorDataResponse {
    private Long id;
    private Long motorId;
    private BigDecimal temperature;
    private BigDecimal vibration;
    private BigDecimal current;
    private String status;
    private LocalDateTime timestamp;
}
