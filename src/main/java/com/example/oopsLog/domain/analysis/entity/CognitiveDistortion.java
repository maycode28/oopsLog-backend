package com.example.oopsLog.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cognitive_distortions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CognitiveDistortion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "distortion_id")
    private Long distortionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correction_id", nullable = false)
    private AiCorrection aiCorrection;

    @Column(name = "distortion_type", length = 100)
    private String distortionType;

    public CognitiveDistortion(AiCorrection aiCorrection, String distortionType) {
        this.aiCorrection = aiCorrection;
        this.distortionType = distortionType;
    }
}