package com.example.oopsLog.domain.analysis.dto.response;

import lombok.Getter;

@Getter
public class DistortionCardResponse {
    private final String label;
    private final String mistakenText;
    private final String reframedText;

    public DistortionCardResponse(String label, String mistakenText, String reframedText) {
        this.label = label;
        this.mistakenText = mistakenText;
        this.reframedText = reframedText;
    }
}