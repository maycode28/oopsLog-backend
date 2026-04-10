package com.example.oopsLog.domain.analysis.entity;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Entity
@Table(name = "distortion_cards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DistortionCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correction_id", nullable = false)
    private AiCorrection aiCorrection;

    @Column(name = "distortion_label", length = 100, nullable = false)
    private String distortionLabel;

    @Column(name = "mistaken_text", columnDefinition = "TEXT", nullable = false)
    private String mistakenText;

    @Column(name = "reframed_text", columnDefinition = "TEXT", nullable = false)
    private String reframedText;

    @Column(name = "card_order", nullable = false)
    private int cardOrder;

    public DistortionCard(AiCorrection aiCorrection, String distortionLabel,
                          String mistakenText, String reframedText, int cardOrder) {
        this.aiCorrection = aiCorrection;
        this.distortionLabel = distortionLabel;
        this.mistakenText = mistakenText;
        this.reframedText = reframedText;
        this.cardOrder = cardOrder;
    }
}