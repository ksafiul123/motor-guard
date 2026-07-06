// ─── ThresholdRepository.java ─────────────────────────────────────────────────
package com.predictive.maintenance.repository;

import com.predictive.maintenance.entity.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThresholdRepository extends JpaRepository<Threshold, Long> {
    Optional<Threshold> findByMotorId(Long motorId);
}