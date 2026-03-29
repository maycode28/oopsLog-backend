package com.example.oopsLog.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AnalysisResponse {

    // Getters & Setters
    private Long id;
    private String text;
    private String summary;
    private String emotion;
    private List<String> bias;
    private String reframe;
    private List<String> actions;

    public AnalysisResponse() {}

}
