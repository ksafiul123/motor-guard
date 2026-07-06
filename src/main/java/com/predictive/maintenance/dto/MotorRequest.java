package com.predictive.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// ─── Motor creation request ───────────────────────────────────────────────────
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotorRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String location;
}
