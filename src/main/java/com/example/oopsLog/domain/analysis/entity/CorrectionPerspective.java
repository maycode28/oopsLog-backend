package com.example.oopsLog.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "correction_perspectives")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CorrectionPerspective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "perspective_id")
    private Long perspectiveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correction_id", nullable = false)
    private AiCorrection aiCorrection;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    public CorrectionPerspective(AiCorrection aiCorrection, String content) {
        this.aiCorrection = aiCorrection;
        this.content = content;
    }
}