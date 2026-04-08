package com.example.oopsLog.domain.analysis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AnalysisUpdateRequest(
        @NotBlank(message = "실패 내용은 비어 있을 수 없습니다.")
        String failureContent,
        String coreInterpretation,
        String gardenerMessage,
        String facts,
        String deconstructionFact,
        String deconstructionInterpretation,
        @NotEmpty(message = "인지 왜곡 목록은 최소 1개 이상이어야 합니다.")
        List<@NotBlank(message = "인지 왜곡 값은 비어 있을 수 없습니다.") String> distortions,
        @NotEmpty(message = "관점 목록은 최소 1개 이상이어야 합니다.")
        List<@NotBlank(message = "관점 값은 비어 있을 수 없습니다.") String> perspectives
) {
}

