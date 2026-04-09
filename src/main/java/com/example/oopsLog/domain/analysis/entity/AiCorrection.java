package com.example.oopsLog.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "ai_corrections")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiCorrection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "correction_id")
    private Long correctionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "failure_id", nullable = false)
    private Failure failure;

    @Column(length = 255)
    private String title;

    @Column(name = "core_interpretation", columnDefinition = "TEXT")
    private String coreInterpretation;

    @Column(name = "analysis_message", columnDefinition = "TEXT")
    private String analysisMessage;

    @Column(columnDefinition = "TEXT")
    private String facts;

    @Column(name = "deconstruction_fact", columnDefinition = "TEXT")
    private String deconstructionFact;

    @Column(name = "deconstruction_interpretation", columnDefinition = "TEXT")
    private String deconstructionInterpretation;

    @OneToMany(mappedBy = "aiCorrection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CognitiveDistortion> distortions = new ArrayList<>();

    @OneToMany(mappedBy = "aiCorrection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CorrectionPerspective> perspectives = new ArrayList<>();

    @Builder
    public AiCorrection(Failure failure, String title, String coreInterpretation,
                        String analysisMessage, String facts,
                        String deconstructionFact, String deconstructionInterpretation) {
        this.failure = failure;
        this.title = title;
        this.coreInterpretation = coreInterpretation;
        this.analysisMessage = analysisMessage;
        this.facts = facts;
        this.deconstructionFact = deconstructionFact;
        this.deconstructionInterpretation = deconstructionInterpretation;
    }

    public void addDistortion(String distortionType) {
        this.distortions.add(new CognitiveDistortion(this, distortionType));
    }

    public void addPerspective(String content) {
        this.perspectives.add(new CorrectionPerspective(this, content));
    }
}