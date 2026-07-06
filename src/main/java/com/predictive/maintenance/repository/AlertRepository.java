// ─── AlertRepository.java ─────────────────────────────────────────────────────
package com.predictive.maintenance.repository;

import com.predictive.maintenance.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // All alerts for a motor, newest first
    List<Alert> findByMotorIdOrderByTimestampDesc(Long motorId);

    // Only unresolved (active) alerts
    List<Alert> findByMotorIdAndResolvedFalseOrderByTimestampDesc(Long motorId);

    // All unresolved alerts across all motors
    List<Alert> findByResolvedFalseOrderByTimestampDesc();

    // Count of active alerts for a motor
    long countByMotorIdAndResolvedFalse(Long motorId);
}

