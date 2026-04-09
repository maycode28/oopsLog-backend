package com.example.oopsLog.domain.analysis.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AnalysisRequest {

    private String text;

    public AnalysisRequest() {}

    public AnalysisRequest(String text) {
        this.text = text;
    }

}
