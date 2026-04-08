package com.example.oopsLog.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cognitive_distortions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisDistortion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "distortion_id")
    private Long distortionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correction_id", nullable = false)
    private AnalysisCorrection correction;

    @Column(name = "distortion_type", length = 100)
    private String distortionType;

    public AnalysisDistortion(AnalysisCorrection correction, String distortionType) {
        this.correction = correction;
        this.distortionType = distortionType;
    }
}

