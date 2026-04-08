package com.example.oopsLog.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "correction_perspectives")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisPerspective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "perspective_id")
    private Long perspectiveId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correction_id", nullable = false)
    private AnalysisCorrection correction;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public AnalysisPerspective(AnalysisCorrection correction, String content) {
        this.correction = correction;
        this.content = content;
    }
}

