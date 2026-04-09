// FailureDetailResponse.java — 상세용
package com.example.oopsLog.domain.analysis.dto.response;

import com.example.oopsLog.domain.analysis.entity.AiCorrection;
import com.example.oopsLog.domain.analysis.entity.Failure;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public class FailureDetailResponse {

    private final Long failureId;
    private final String content;
    private final LocalDateTime createdAt;

    // AiCorrection fields
    private final String title;
    private final String coreInterpretation;
    private final String analysisMessage;
    private final List<String> facts;
    private final String deconstructionFact;
    private final String deconstructionInterpretation;
    private final List<String> distortions;
    private final List<String> perspectives;

    public FailureDetailResponse(Failure failure) {
        this.failureId = failure.getFailureId();
        this.content = failure.getContent();
        this.createdAt = failure.getCreatedAt();

        AiCorrection c = failure.getAiCorrection();
        if (c != null) {
            this.title = c.getTitle();
            this.coreInterpretation = c.getCoreInterpretation();
            this.analysisMessage = c.getAnalysisMessage();
            this.facts = c.getFacts() != null
                    ? Arrays.asList(c.getFacts().split(","))
                    : Collections.emptyList();
            this.deconstructionFact = c.getDeconstructionFact();
            this.deconstructionInterpretation = c.getDeconstructionInterpretation();
            this.distortions = c.getDistortions().stream()
                    .map(d -> d.getDistortionType()).toList();
            this.perspectives = c.getPerspectives().stream()
                    .map(p -> p.getContent()).toList();
        } else {
            this.title = null;
            this.coreInterpretation = null;
            this.analysisMessage = null;
            this.facts = Collections.emptyList();
            this.deconstructionFact = null;
            this.deconstructionInterpretation = null;
            this.distortions = Collections.emptyList();
            this.perspectives = Collections.emptyList();
        }
    }
}