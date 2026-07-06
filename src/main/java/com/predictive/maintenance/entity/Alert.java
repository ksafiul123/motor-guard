package com.predictive.maintenance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_motor_id",  columnList = "motor_id"),
        @Index(name = "idx_alert_timestamp", columnList = "timestamp")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motor_id", nullable = false)
    private Motor motor;

    @Column(nullable = false, length = 30)
    private String type;        // OVERHEAT | VIBRATION | OVERLOAD

    @Column(nullable = false, length = 20)
    private String severity;    // WARNING | CRITICAL

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private Boolean resolved = false;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}