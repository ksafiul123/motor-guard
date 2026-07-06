// ─── MotorRepository.java ─────────────────────────────────────────────────────
package com.predictive.maintenance.repository;

import com.predictive.maintenance.entity.Motor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MotorRepository extends JpaRepository<Motor, Long> {
}