package com.example.oopsLog.domain.analysis.repository;

import com.example.oopsLog.domain.analysis.entity.Failure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailureRepository extends JpaRepository<Failure, Long> {
    List<Failure> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
}