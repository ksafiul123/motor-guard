package com.predictive.maintenance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "thresholds")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Threshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motor_id", nullable = false, unique = true)
    private Motor motor;

    @Column(name = "max_temperature", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal maxTemperature = new BigDecimal("70.00");

    @Column(name = "max_vibration", nullable = false, precision = 8, scale = 4)
    @Builder.Default
    private BigDecimal maxVibration = new BigDecimal("5.0000");

    @Column(name = "max_current", nullable = false, precision = 6, scale = 3)
    @Builder.Default
    private BigDecimal maxCurrent = new BigDecimal("4.500");

    // Warning thresholds (80% of critical by default)
    @Column(name = "warn_temperature", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal warnTemperature = new BigDecimal("55.00");

    @Column(name = "warn_vibration", nullable = false, precision = 8, scale = 4)
    @Builder.Default
    private BigDecimal warnVibration = new BigDecimal("2.0000");

    @Column(name = "warn_current", nullable = false, precision = 6, scale = 3)
    @Builder.Default
    private BigDecimal warnCurrent = new BigDecimal("3.000");
}