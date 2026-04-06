package com.example.oopsLog.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
@Getter
public class AnalysisResponse {

    private Analysis analysis;
    private Deconstruction deconstruction;
    private List<String> perspectives;

    @JsonProperty("gardener_message")
    private String gardenerMessage;

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
