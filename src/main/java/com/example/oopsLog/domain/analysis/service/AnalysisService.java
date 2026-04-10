package com.example.oopsLog.domain.analysis.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

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

    // 분석 실행 + 저장
    @Transactional
    public AnalysisResponse analyze(Long userId, String text) throws Exception {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new IllegalStateException("Missing Gemini API key.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 1. Failure 저장
        Failure failure = new Failure(user, text);
        failureRepository.save(failure);

        // 2. Gemini 호출
        AnalysisResponse aiResponse = callGemini(text);

        // 3. AiCorrection 저장
        AnalysisResponse.Analysis analysis = aiResponse.getAnalysis();
        AnalysisResponse.Deconstruction deconstruction = aiResponse.getDeconstruction();

        AiCorrection correction = AiCorrection.builder()
                .failure(failure)
                .title(aiResponse.getTitle())
                .coreInterpretation(analysis != null ? analysis.getCoreInterpretation() : null)
                .analysisMessage(aiResponse.getAnalysisMessage())
                .facts(analysis != null && analysis.getFacts() != null
                        ? String.join(",", analysis.getFacts()) : null)
                .deconstructionFact(deconstruction != null ? deconstruction.getFact() : null)
                .deconstructionInterpretation(deconstruction != null ? deconstruction.getInterpretation() : null)
                .build();

        if (analysis != null && analysis.getDistortions() != null) {
            analysis.getDistortions().forEach(correction::addDistortion);
        }
        if (aiResponse.getPerspectives() != null) {
            aiResponse.getPerspectives().forEach(correction::addPerspective);
        }

        aiCorrectionRepository.save(correction);

        return aiResponse;
    }

    // 특정 유저의 실패 기록 목록
    public List<FailureListResponse> getFailureList(Long userId) {
        return failureRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(FailureListResponse::new)
                .collect(Collectors.toList());
    }

    // 특정 실패 기록 상세
    public FailureDetailResponse getFailureDetail(Long userId, Long failureId) {
        Failure failure = failureRepository.findById(failureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다."));

        if (!failure.getUser().getUserId().equals(userId)) {
            throw new SecurityException("접근 권한이 없습니다.");
        }

        return new FailureDetailResponse(failure);
    }

    // Gemini API 호출
    private AnalysisResponse callGemini(String text) throws Exception {
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
                        "temperature", 0.2,
                        "maxOutputTokens", 4096,
                        "responseMimeType", "application/json"
                )
        );

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel + ":generateContent?key=" + geminiApiKey;

        ResponseEntity<String> response = postWithRetry(
                url, new HttpEntity<>(requestBody, headers), String.class);

        if (response.getBody() == null || response.getBody().isBlank()) {
            throw new IllegalStateException("Gemini 응답 본문이 비어 있습니다.");
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        validateGeminiResponse(root);

        String rawText = extractGeminiText(root);
        String json = extractJsonObject(rawText);

        return objectMapper.readValue(json, AnalysisResponse.class);
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
                if (!retryable || attempt == attempts) throw e;

                sleepBackoff(attempt);
            }
        }
        throw lastException != null ? lastException : new IllegalStateException("Gemini 호출에 실패했습니다.");
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
            throw new IllegalStateException("Gemini 응답에 candidates가 없습니다.");
        }
        String finishReason = candidates.get(0).path("finishReason").asText("");
        if ("MAX_TOKENS".equals(finishReason)) {
            throw new IllegalStateException("Gemini 응답이 maxOutputTokens 제한에 걸렸습니다.");
        }
        if ("SAFETY".equals(finishReason)) {
            throw new IllegalStateException("Gemini 응답이 safety 정책으로 차단되었습니다.");
        }
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            throw new IllegalStateException("Gemini 응답 content.parts가 비어 있습니다.");
        }
    }

    private String extractGeminiText(JsonNode root) {
        JsonNode parts = root.path("candidates").get(0).path("content").path("parts");
        StringBuilder sb = new StringBuilder();
        for (JsonNode part : parts) {
            if (part.has("text")) sb.append(part.path("text").asText(""));
        }
        return sb.toString().trim();
    }

    private String extractJsonObject(String content) {
        if (content == null || content.isBlank()) return "{}";
        String cleaned = content.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        return (start >= 0 && end > start) ? cleaned.substring(start, end + 1).trim() : cleaned;
    }
}
