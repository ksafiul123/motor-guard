package com.predictive.maintenance.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

// ─── Incoming from ESP32 ──────────────────────────────────────────────────────
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SensorDataRequest {

    @NotNull(message = "motorId is required")
    private Long motorId;

    @NotNull @DecimalMin("0.0") @DecimalMax("150.0")
    private BigDecimal temperature;

    @NotNull @DecimalMin("0.0")
    private BigDecimal vibration;

    @NotNull
    private BigDecimal current;

    private String status;   // optional — backend re-evaluates anyway
}

