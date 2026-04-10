// FailureDetailResponse.java — 상세용
package com.example.oopsLog.domain.analysis.dto.response;

import com.example.oopsLog.domain.analysis.entity.AiCorrection;
import com.example.oopsLog.domain.analysis.entity.Failure;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
public class FailureDetailResponse {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Long failureId;
    private final String content;
    private final LocalDateTime createdAt;

    private final String title;
    private final String analysisMessage;
    private final List<String> facts;
    private final List<DistortionCardResponse> distortionCards;

    public FailureDetailResponse(Failure failure) {
        this.failureId = failure.getFailureId();
        this.content = failure.getContent();
        this.createdAt = failure.getCreatedAt();

        AiCorrection c = failure.getAiCorrection();
        if (c != null) {
            this.title = c.getTitle();
            this.analysisMessage = c.getAnalysisMessage();
            this.facts = parseFacts(c.getFacts());
            this.distortionCards = c.getDistortionCards() != null
                    ? c.getDistortionCards().stream()
                    .map(card -> new DistortionCardResponse(
                            card.getDistortionLabel(),
                            card.getMistakenText(),
                            card.getReframedText()
                    ))
                    .toList()
                    : Collections.emptyList();
        } else {
            this.title = null;
            this.analysisMessage = null;
            this.facts = Collections.emptyList();
            this.distortionCards = Collections.emptyList();
        }
    }

    private List<String> parseFacts(String factsJson) {
        if (factsJson == null || factsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(factsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}