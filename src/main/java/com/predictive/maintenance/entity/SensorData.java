package com.predictive.maintenance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data", indexes = {
        @Index(name = "idx_sensor_motor_id",  columnList = "motor_id"),
        @Index(name = "idx_sensor_timestamp", columnList = "timestamp")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motor_id", nullable = false)
    private Motor motor;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal temperature;          // °C

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal vibration;            // m/s²

    @Column(nullable = false, precision = 6, scale = 3)
    private BigDecimal current;              // Amperes

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "NORMAL";        // evaluated status at time of reading

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}