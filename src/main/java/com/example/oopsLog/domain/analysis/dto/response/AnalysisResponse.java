package com.example.oopsLog.domain.analysis.dto.response;

import com.example.oopsLog.domain.analysis.entity.AnalysisCorrection;

import java.util.List;

public record AnalysisResponse(
        Long failureId,
        Long userId,
        String failureContent,
        Long correctionId,
        String coreInterpretation,
        String gardenerMessage,
        String facts,
        String deconstructionFact,
        String deconstructionInterpretation,
        List<String> distortions,
        List<String> perspectives
) {
    public static AnalysisResponse from(AnalysisCorrection correction) {
        return new AnalysisResponse(
                correction.getFailure().getFailureId(),
                correction.getFailure().getUser().getUserId(),
                correction.getFailure().getContent(),
                correction.getCorrectionId(),
                correction.getCoreInterpretation(),
                correction.getGardenerMessage(),
                correction.getFacts(),
                correction.getDeconstructionFact(),
                correction.getDeconstructionInterpretation(),
                correction.getDistortions().stream().map(d -> d.getDistortionType()).toList(),
                correction.getPerspectives().stream().map(p -> p.getContent()).toList()
        );
    }
}

