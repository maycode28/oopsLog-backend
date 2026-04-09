package com.example.oopsLog.domain.analysis.repository;

import com.example.oopsLog.domain.analysis.entity.AiCorrection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiCorrectionRepository extends JpaRepository<AiCorrection, Long> {
    Optional<AiCorrection> findByFailure_FailureId(Long failureId);
}