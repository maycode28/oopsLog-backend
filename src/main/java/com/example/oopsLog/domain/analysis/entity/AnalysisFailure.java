package com.example.oopsLog.domain.analysis.entity;

import com.example.oopsLog.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "failures")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "failure_id")
    private Long failureId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "failure", cascade = CascadeType.ALL, orphanRemoval = true)
    private AnalysisCorrection correction;

    public AnalysisFailure(User user, String content) {
        this.user = user;
        this.content = content;
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void updateContent(String content) {
        this.content = content;
    }
}

