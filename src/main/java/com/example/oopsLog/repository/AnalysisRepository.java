package com.example.oopsLog.repository;


import com.example.oopsLog.entity.AnalysisRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// =============================================
// AnalysisRepository.java
// Spring Data JPA가 자동으로 CRUD 구현을 생성해줍니다.
// findAll(), save() 등을 바로 사용할 수 있습니다.
// =============================================
@Repository
public interface AnalysisRepository extends JpaRepository<AnalysisRecord, Long> {
    // 기본 제공: findAll(), findById(), save(), deleteById() 등
    // 추가 쿼리가 필요하면 여기에 메서드 선언만 하면 됩니다.
}