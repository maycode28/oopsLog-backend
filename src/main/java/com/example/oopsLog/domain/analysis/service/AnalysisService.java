package com.example.oopsLog.domain.analysis.service;

import com.example.oopsLog.common.exception.CustomException;
import com.example.oopsLog.common.exception.ErrorCode;
import com.example.oopsLog.common.llm.LlmRequest;
import com.example.oopsLog.common.llm.router.LlmOrchestrator;
import com.example.oopsLog.domain.analysis.dto.response.AnalysisResponse;
import com.example.oopsLog.domain.analysis.dto.response.FailureDetailResponse;
import com.example.oopsLog.domain.analysis.dto.response.FailureListResponse;
import com.example.oopsLog.domain.analysis.entity.AiCorrection;
import com.example.oopsLog.domain.analysis.entity.Failure;
import com.example.oopsLog.domain.analysis.prompt.AiPromptBuilder;
import com.example.oopsLog.domain.analysis.repository.AiCorrectionRepository;
import com.example.oopsLog.domain.analysis.repository.FailureRepository;
import com.example.oopsLog.domain.user.entity.User;
import com.example.oopsLog.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalysisService {

    private final FailureRepository failureRepository;
    private final AiCorrectionRepository aiCorrectionRepository;
    private final UserRepository userRepository;
    private final AiPromptBuilder promptBuilder;
    private final LlmOrchestrator llmOrchestrator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisService(FailureRepository failureRepository,
                           AiCorrectionRepository aiCorrectionRepository,
                           UserRepository userRepository,
                           AiPromptBuilder promptBuilder,
                           LlmOrchestrator llmOrchestrator) {
        this.failureRepository = failureRepository;
        this.aiCorrectionRepository = aiCorrectionRepository;
        this.userRepository = userRepository;
        this.promptBuilder = promptBuilder;
        this.llmOrchestrator = llmOrchestrator;
    }

    @Transactional
    public AnalysisResponse analyze(Long userId, String text) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Failure failure = new Failure(user, text);
        failureRepository.save(failure);

        try {
            AnalysisResponse aiResponse = callLlm(userId, text);

            AiCorrection correction = AiCorrection.builder()
                    .failure(failure)
                    .title(aiResponse.getTitle())
                    .analysisMessage(aiResponse.getAnalysisMessage())
                    .facts(aiResponse.getFacts() != null
                            ? objectMapper.writeValueAsString(aiResponse.getFacts())
                            : null)
                    .build();

            if (aiResponse.getFlipCards() != null) {
                for (int i = 0; i < aiResponse.getFlipCards().size(); i++) {
                    AnalysisResponse.FlipCard card = aiResponse.getFlipCards().get(i);
                    correction.addDistortionCard(
                            card.getLabel(),
                            card.getMistaken(),
                            card.getReframed(),
                            i
                    );
                }
            }

            aiCorrectionRepository.save(correction);
            return aiResponse;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public List<FailureListResponse> getFailureList(Long userId) {
        return failureRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(FailureListResponse::new)
                .collect(Collectors.toList());
    }

    public FailureDetailResponse getFailureDetail(Long userId, Long failureId) {
        Failure failure = failureRepository.findById(failureId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANALYSIS_NOT_FOUND));

        if (!failure.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return new FailureDetailResponse(failure);
    }

    private AnalysisResponse callLlm(Long userId, String text) {
        try {
            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(text);

            String rawText = llmOrchestrator.generate(new LlmRequest(systemPrompt, userPrompt), userId).text();
            String json = extractJsonObject(rawText);

            return objectMapper.readValue(json, AnalysisResponse.class);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractJsonObject(String content) {
        if (content == null || content.isBlank()) {
            return "{}";
        }

        String cleaned = content.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        return (start >= 0 && end > start)
                ? cleaned.substring(start, end + 1).trim()
                : cleaned;
    }
}
