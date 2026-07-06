package com.predictive.maintenance.service;

import com.predictive.maintenance.entity.*;
import com.predictive.maintenance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository    alertRepository;
    private final ThresholdRepository thresholdRepository;
    private final SensorDataRepository sensorDataRepository;

    // ─── Evaluate incoming reading against thresholds ─────────────────────────
    public List<Alert> evaluate(Motor motor, SensorData data) {
        List<Alert> generated = new ArrayList<>();

        Threshold threshold = thresholdRepository.findByMotorId(motor.getId())
                .orElse(defaultThreshold(motor));

        // Temperature check
        Alert tempAlert = checkValue(
                motor,
                data.getTemperature(),
                threshold.getWarnTemperature(),
                threshold.getMaxTemperature(),
                "OVERHEAT",
                "°C"
        );
        if (tempAlert != null) generated.add(tempAlert);

        // Vibration check
        Alert vibAlert = checkValue(
                motor,
                data.getVibration(),
                threshold.getWarnVibration(),
                threshold.getMaxVibration(),
                "VIBRATION",
                "m/s²"
        );
        if (vibAlert != null) generated.add(vibAlert);

        // Current check
        Alert currAlert = checkValue(
                motor,
                data.getCurrent(),
                threshold.getWarnCurrent(),
                threshold.getMaxCurrent(),
                "OVERLOAD",
                "A"
        );
        if (currAlert != null) generated.add(currAlert);

        // Trend analysis — gradual vibration increase = predictive warning
        Alert trendAlert = checkVibrationTrend(motor, data.getVibration());
        if (trendAlert != null) generated.add(trendAlert);

        // Save all generated alerts
        if (!generated.isEmpty()) {
            alertRepository.saveAll(generated);
            log.warn("[ALERT] {} alert(s) generated for motor '{}'",
                    generated.size(), motor.getName());
        }

        return generated;
    }

    // ─── Threshold comparison helper ──────────────────────────────────────────
    private Alert checkValue(Motor motor, BigDecimal value,
                             BigDecimal warn, BigDecimal critical,
                             String type, String unit) {
        String severity = null;

        if (value.compareTo(critical) >= 0) {
            severity = "CRITICAL";
        } else if (value.compareTo(warn) >= 0) {
            severity = "WARNING";
        }

        if (severity == null) return null;

        String message = buildMessage(type, severity, value, unit,
                severity.equals("CRITICAL") ? critical : warn);

        return Alert.builder()
                .motor(motor)
                .type(type)
                .severity(severity)
                .message(message)
                .resolved(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ─── Predictive trend: rising vibration over last 10 readings ────────────
    // If the last 10 readings show a consistent upward trend → predictive alert
    private Alert checkVibrationTrend(Motor motor, BigDecimal currentVib) {
        List<SensorData> recent = sensorDataRepository
                .findByMotorIdOrderByTimestampDesc(motor.getId(),
                        org.springframework.data.domain.PageRequest.of(0, 10));

        if (recent.size() < 5) return null;   // not enough data yet

        // Calculate slope: are readings consistently increasing?
        int risingCount = 0;
        for (int i = 0; i < recent.size() - 1; i++) {
            if (recent.get(i).getVibration()
                    .compareTo(recent.get(i + 1).getVibration()) > 0) {
                risingCount++;
            }
        }

        // 80% of readings are rising = trend alert
        double trendRatio = (double) risingCount / (recent.size() - 1);
        if (trendRatio >= 0.80) {
            log.warn("[TREND] Rising vibration trend detected for motor '{}'", motor.getName());
            return Alert.builder()
                    .motor(motor)
                    .type("VIBRATION")
                    .severity("WARNING")
                    .message(String.format(
                            "Predictive alert: Vibration showing consistent upward trend " +
                                    "(%.0f%% of last %d readings increasing). " +
                                    "Current: %.4f m/s². Schedule maintenance soon.",
                            trendRatio * 100, recent.size(), currentVib))
                    .resolved(false)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        return null;
    }

    // ─── Determine overall motor status ──────────────────────────────────────
    public String determineStatus(SensorData data, Threshold threshold) {
        if (data.getTemperature().compareTo(threshold.getMaxTemperature()) >= 0 ||
                data.getVibration().compareTo(threshold.getMaxVibration())    >= 0 ||
                data.getCurrent().compareTo(threshold.getMaxCurrent())        >= 0) {
            return "CRITICAL";
        }
        if (data.getTemperature().compareTo(threshold.getWarnTemperature()) >= 0 ||
                data.getVibration().compareTo(threshold.getWarnVibration())    >= 0 ||
                data.getCurrent().compareTo(threshold.getWarnCurrent())        >= 0) {
            return "WARNING";
        }
        return "NORMAL";
    }

    // ─── Resolve an alert manually ────────────────────────────────────────────
    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        alert.setResolved(true);
        return alertRepository.save(alert);
    }

    // ─── Message builder ──────────────────────────────────────────────────────
    private String buildMessage(String type, String severity,
                                BigDecimal value, String unit, BigDecimal limit) {
        return switch (type) {
            case "OVERHEAT"  -> String.format("%s: Temperature %.2f%s exceeds %s limit of %.2f%s",
                    severity, value, unit, severity.toLowerCase(), limit, unit);
            case "VIBRATION" -> String.format("%s: Vibration %.4f%s exceeds %s limit of %.4f%s",
                    severity, value, unit, severity.toLowerCase(), limit, unit);
            case "OVERLOAD"  -> String.format("%s: Current %.3f%s exceeds %s limit of %.3f%s",
                    severity, value, unit, severity.toLowerCase(), limit, unit);
            default          -> String.format("%s: %s value %.4f%s exceeded",
                    severity, type, value, unit);
        };
    }

    // ─── Default thresholds if none configured ────────────────────────────────
    private Threshold defaultThreshold(Motor motor) {
        return Threshold.builder().motor(motor).build();
    }
}