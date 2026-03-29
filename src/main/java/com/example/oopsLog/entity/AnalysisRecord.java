package com.example.oopsLog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

// =============================================
// AnalysisRecord.java - DB 테이블과 매핑되는 Entity
// H2 메모리 DB에 분석 결과를 저장합니다.
// =============================================
@Setter
@Getter
@Entity
@Table(name = "analysis_records")
public class AnalysisRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자가 입력한 원문 (긴 텍스트를 위해 @Column(length) 설정)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 500)
    private String emotion;

    // bias는 여러 항목을 ','로 이어서 하나의 문자열로 저장
    // (실무에서는 별도 테이블을 쓰지만, MVP에서는 이렇게 단순하게 처리)
    @Column(columnDefinition = "TEXT")
    private String biasRaw;   // DB 저장용 (콤마로 이어진 문자열)

    @Column(columnDefinition = "TEXT")
    private String reframe;

    @Column(columnDefinition = "TEXT")
    private String actionsRaw; // DB 저장용 (콤마로 이어진 문자열)

    // ── Getter / Setter ──────────────────────

    // bias를 List<String>으로 변환하는 편의 메서드
    public List<String> getBias() {
        if (biasRaw == null || biasRaw.isBlank()) return List.of();
        return Arrays.stream(biasRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    // actions를 List<String>으로 변환하는 편의 메서드
    public List<String> getActions() {
        if (actionsRaw == null || actionsRaw.isBlank()) return List.of();
        return Arrays.stream(actionsRaw.split("\\|"))// 파이프(|)로 구분 (콤마는 문장 안에 들어갈 수 있음)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}