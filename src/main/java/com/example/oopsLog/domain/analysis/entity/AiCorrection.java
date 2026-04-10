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

    @Column(name = "analysis_message", columnDefinition = "TEXT")
    private String analysisMessage;

    @Column(columnDefinition = "TEXT")
    private String facts;

    @OneToMany(mappedBy = "aiCorrection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DistortionCard> distortionCards = new ArrayList<>();

    @Builder
    public AiCorrection(Failure failure, String title, String analysisMessage, String facts) {
        this.failure = failure;
        this.title = title;
        this.analysisMessage = analysisMessage;
        this.facts = facts;
    }

    public void addDistortionCard(String label, String mistakenText, String reframedText, int cardOrder) {
        this.distortionCards.add(new DistortionCard(this, label, mistakenText, reframedText, cardOrder));
    }
}