package com.example.oopsLog.domain.analysis.repository;

import com.example.oopsLog.domain.analysis.entity.AnalysisFailure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisFailureRepository extends JpaRepository<AnalysisFailure, Long> {
    List<AnalysisFailure> findByUserUserId(Long userId);
}

