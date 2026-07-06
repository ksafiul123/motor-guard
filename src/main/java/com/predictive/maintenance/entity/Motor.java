package com.predictive.maintenance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "motors")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Motor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "NORMAL";   // NORMAL | WARNING | CRITICAL

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "motor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SensorData> sensorDataList;

    @OneToMany(mappedBy = "motor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Alert> alerts;

    @OneToOne(mappedBy = "motor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Threshold threshold;
}