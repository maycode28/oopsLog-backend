package com.example.oopsLog.domain.analysis.service;

import com.example.oopsLog.common.exception.CustomException;
import com.example.oopsLog.common.exception.ErrorCode;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalysisService {

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    private final int geminiRetryMaxAttempts;

    private final FailureRepository failureRepository;
    private final AiCorrectionRepository aiCorrectionRepository;
    private final UserRepository userRepository;
    private final AiPromptBuilder promptBuilder;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisService(FailureRepository failureRepository,
                           AiCorrectionRepository aiCorrectionRepository,
                           UserRepository userRepository,
                           AiPromptBuilder promptBuilder,
                           @Value("${gemini.timeout.connect-ms:5000}") int geminiConnectTimeoutMs,
                           @Value("${gemini.timeout.read-ms:30000}") int geminiReadTimeoutMs,
                           @Value("${gemini.retry.max-attempts:4}") int geminiRetryMaxAttempts) {
        this.failureRepository = failureRepository;
        this.aiCorrectionRepository = aiCorrectionRepository;
        this.userRepository = userRepository;
        this.promptBuilder = promptBuilder;
        this.geminiRetryMaxAttempts = geminiRetryMaxAttempts;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(geminiConnectTimeoutMs);
        factory.setReadTimeout(geminiReadTimeoutMs);
        this.restTemplate = new RestTemplate(factory);
    }

    @Transactional
    public AnalysisResponse analyze(Long userId, String text) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Failure failure = new Failure(user, text);
        failureRepository.save(failure);

        try {
            AnalysisResponse aiResponse = callGemini(text);

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

        } catch (HttpStatusCodeException e) {
            throw e;
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

    private AnalysisResponse callGemini(String text) {
        try {
            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "systemInstruction", Map.of(
                            "parts", List.of(Map.of("text", systemPrompt))
                    ),
                    "contents", List.of(Map.of(
                            "role", "user",
                            "parts", List.of(Map.of("text", userPrompt))
                    )),
                    "generationConfig", Map.of(
                            "temperature", 0.55,
                            "topP", 0.9,
                            "topK", 40,
                            "maxOutputTokens", 2048,
                            "responseMimeType", "application/json"
                    )
            );

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + geminiModel + ":generateContent?key=" + geminiApiKey;

            ResponseEntity<String> response = postWithRetry(
                    url, new HttpEntity<>(requestBody, headers), String.class);

            if (response.getBody() == null || response.getBody().isBlank()) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            validateGeminiResponse(root);

            String rawText = extractGeminiText(root);
            String json = extractJsonObject(rawText);

            return objectMapper.readValue(json, AnalysisResponse.class);

        } catch (HttpStatusCodeException | CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private <T> ResponseEntity<T> postWithRetry(String url, HttpEntity<?> request, Class<T> responseType) {
        int attempts = Math.max(1, geminiRetryMaxAttempts);
        HttpStatusCodeException lastException = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return restTemplate.postForEntity(url, request, responseType);
            } catch (HttpStatusCodeException e) {
                lastException = e;
                int status = e.getStatusCode().value();
                boolean retryable = status == 429 || status == 500 || status == 502 || status == 503 || status == 504;
                if (!retryable || attempt == attempts) {
                    throw e;
                }
                sleepBackoff(attempt);
            }
        }

        throw lastException != null
                ? lastException
                : new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private void sleepBackoff(int attempt) {
        long baseMs = 400L;
        long maxMs = 4000L;
        long exp = (long) (baseMs * Math.pow(2, Math.max(0, attempt - 1)));
        long jitter = ThreadLocalRandom.current().nextLong(0, 250);
        long sleepMs = Math.min(maxMs, exp + jitter);
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void validateGeminiResponse(JsonNode root) {
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        String finishReason = candidates.get(0).path("finishReason").asText("");
        if ("MAX_TOKENS".equals(finishReason)) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        if ("SAFETY".equals(finishReason)) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractGeminiText(JsonNode root) {
        JsonNode parts = root.path("candidates").get(0).path("content").path("parts");
        StringBuilder sb = new StringBuilder();
        for (JsonNode part : parts) {
            if (part.has("text")) {
                sb.append(part.path("text").asText(""));
            }
        }
        return sb.toString().trim();
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