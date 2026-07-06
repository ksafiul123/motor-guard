package com.predictive.maintenance.dto;

import lombok.*;

import java.time.LocalDateTime;

// ─── Alert response ───────────────────────────────────────────────────────────
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponse {
    private Long id;
    private Long motorId;
    private String motorName;
    private String type;
    private String severity;
    private String message;
    private Boolean resolved;
    private LocalDateTime timestamp;
}
