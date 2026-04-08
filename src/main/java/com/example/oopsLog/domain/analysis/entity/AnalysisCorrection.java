package com.example.oopsLog.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "ai_corrections")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisCorrection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "correction_id")
    private Long correctionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "failure_id", nullable = false, unique = true)
    private AnalysisFailure failure;

    @Column(name = "core_interpretation", columnDefinition = "TEXT")
    private String coreInterpretation;

    @Column(name = "gardener_message", columnDefinition = "TEXT")
    private String gardenerMessage;

    @Column(columnDefinition = "TEXT")
    private String facts;

    @Column(name = "deconstruction_fact", columnDefinition = "TEXT")
    private String deconstructionFact;

    @Column(name = "deconstruction_interpretation", columnDefinition = "TEXT")
    private String deconstructionInterpretation;

    @OneToMany(mappedBy = "correction", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<AnalysisDistortion> distortions = new ArrayList<>();

    @OneToMany(mappedBy = "correction", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<AnalysisPerspective> perspectives = new ArrayList<>();

    public AnalysisCorrection(
            AnalysisFailure failure,
            String coreInterpretation,
            String gardenerMessage,
            String facts,
            String deconstructionFact,
            String deconstructionInterpretation
    ) {
        this.failure = failure;
        this.coreInterpretation = coreInterpretation;
        this.gardenerMessage = gardenerMessage;
        this.facts = facts;
        this.deconstructionFact = deconstructionFact;
        this.deconstructionInterpretation = deconstructionInterpretation;
    }

    public void updateMain(
            String coreInterpretation,
            String gardenerMessage,
            String facts,
            String deconstructionFact,
            String deconstructionInterpretation
    ) {
        this.coreInterpretation = coreInterpretation;
        this.gardenerMessage = gardenerMessage;
        this.facts = facts;
        this.deconstructionFact = deconstructionFact;
        this.deconstructionInterpretation = deconstructionInterpretation;
    }
}

