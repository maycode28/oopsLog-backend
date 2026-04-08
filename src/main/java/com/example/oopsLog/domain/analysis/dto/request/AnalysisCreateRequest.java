package com.example.oopsLog.domain.analysis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AnalysisCreateRequest(
        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @NotBlank(message = "실패 내용은 비어 있을 수 없습니다.")
        String failureContent,

        @Size(max = 4000, message = "핵심 해석 길이가 너무 깁니다.")
        String coreInterpretation,

        @Size(max = 4000, message = "가드너 메시지 길이가 너무 깁니다.")
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

