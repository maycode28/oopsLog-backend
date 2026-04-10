package com.example.oopsLog.domain.analysis.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
@Getter
public class AnalysisResponse {

    private String title; // 추가: 분석 제목
    private Analysis analysis;
    private Deconstruction deconstruction;
    private List<String> perspectives;

    @JsonProperty("analysis_message") // 변경: 가드너 메시지에서 분석 메시지로
    private String analysisMessage;

    public AnalysisResponse() {}

    @Setter
    @Getter
    public static class Analysis {
        private List<String> distortions;
        @JsonProperty("core_interpretation")
        private String coreInterpretation;
        private List<String> facts;
    }

    @Setter
    @Getter
    public static class Deconstruction {
        private String fact;
        private String interpretation;
        private String distortion;
    }
}