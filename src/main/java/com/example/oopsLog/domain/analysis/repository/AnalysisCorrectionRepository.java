package com.example.oopsLog.domain.analysis.repository;

import com.example.oopsLog.domain.analysis.entity.AnalysisCorrection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisCorrectionRepository extends JpaRepository<AnalysisCorrection, Long> {
    Optional<AnalysisCorrection> findByFailureFailureId(Long failureId);
}

