package com.example.oopsLog.domain.analysis.service;

import com.example.oopsLog.domain.analysis.dto.request.AnalysisCreateRequest;
import com.example.oopsLog.domain.analysis.dto.request.AnalysisUpdateRequest;
import com.example.oopsLog.domain.analysis.entity.AnalysisCorrection;
import com.example.oopsLog.domain.analysis.entity.AnalysisDistortion;
import com.example.oopsLog.domain.analysis.entity.AnalysisFailure;
import com.example.oopsLog.domain.analysis.entity.AnalysisPerspective;
import com.example.oopsLog.domain.analysis.repository.AnalysisCorrectionRepository;
import com.example.oopsLog.domain.user.entity.User;
import com.example.oopsLog.domain.user.repository.UserRepository;
import com.example.oopsLog.common.exception.CustomException;
import com.example.oopsLog.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class AnalysisService {

    private final AnalysisCorrectionRepository correctionRepository;
    private final UserRepository userRepository;

    public AnalysisService(
            AnalysisCorrectionRepository correctionRepository,
            UserRepository userRepository
    ) {
        this.correctionRepository = correctionRepository;
        this.userRepository = userRepository;
    }

    public AnalysisCorrection create(AnalysisCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        AnalysisFailure failure = new AnalysisFailure(user, request.failureContent());
        AnalysisCorrection correction = new AnalysisCorrection(
                failure,
                request.coreInterpretation(),
                request.gardenerMessage(),
                request.facts(),
                request.deconstructionFact(),
                request.deconstructionInterpretation()
        );

        request.distortions().forEach(type ->
                correction.getDistortions().add(new AnalysisDistortion(correction, type)));
        request.perspectives().forEach(content ->
                correction.getPerspectives().add(new AnalysisPerspective(correction, content)));

        return correctionRepository.save(correction);
    }

    public List<AnalysisCorrection> findAll() {
        return correctionRepository.findAll();
    }

    public AnalysisCorrection findById(Long correctionId) {
        return correctionRepository.findById(correctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANALYSIS_NOT_FOUND));
    }

    public AnalysisCorrection update(Long correctionId, AnalysisUpdateRequest request) {
        AnalysisCorrection correction = findById(correctionId);
        correction.getFailure().updateContent(request.failureContent());
        correction.updateMain(
                request.coreInterpretation(),
                request.gardenerMessage(),
                request.facts(),
                request.deconstructionFact(),
                request.deconstructionInterpretation()
        );

        correction.getDistortions().clear();
        request.distortions().forEach(type ->
                correction.getDistortions().add(new AnalysisDistortion(correction, type)));

        correction.getPerspectives().clear();
        request.perspectives().forEach(content ->
                correction.getPerspectives().add(new AnalysisPerspective(correction, content)));

        return correction;
    }

    public void delete(Long correctionId) {
        correctionRepository.delete(findById(correctionId));
    }
}

