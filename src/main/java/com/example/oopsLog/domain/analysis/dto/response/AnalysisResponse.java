package com.example.oopsLog.domain.analysis.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class AnalysisResponse {

    @JsonProperty("ti")
    private String title;

    @JsonProperty("fc")
    private List<FlipCard> flipCards;

    @JsonProperty("fs")
    private List<String> facts;

    @JsonProperty("am")
    private String analysisMessage;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlipCard {
        @JsonProperty("l")
        private String label;

        @JsonProperty("m")
        private String mistaken;

        @JsonProperty("r")
        private String reframed;
    }
}