// ─── SensorDataRepository.java ────────────────────────────────────────────────
package com.predictive.maintenance.repository;

import com.predictive.maintenance.entity.SensorData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // All readings for a motor, newest first
    List<SensorData> findByMotorIdOrderByTimestampDesc(Long motorId);

    // Last N readings for a motor (for dashboard graph)
    List<SensorData> findByMotorIdOrderByTimestampDesc(Long motorId, Pageable pageable);

    // Readings within a time range
    List<SensorData> findByMotorIdAndTimestampBetweenOrderByTimestampAsc(
            Long motorId, LocalDateTime from, LocalDateTime to
    );

    // Most recent single reading
    Optional<SensorData> findTopByMotorIdOrderByTimestampDesc(Long motorId);

    // Trend analysis: average values over last N readings
    @Query("""
        SELECT AVG(s.temperature), AVG(s.vibration), AVG(s.current)
        FROM SensorData s
        WHERE s.motor.id = :motorId
        AND s.timestamp >= :since
    """)
    Object[] getAveragesSince(Long motorId, LocalDateTime since);
}

