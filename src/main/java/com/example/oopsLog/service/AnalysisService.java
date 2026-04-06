package com.example.oopsLog.service;

import com.example.oopsLog.dto.response.AnalysisResponse;
import com.example.oopsLog.entity.AnalysisRecord;
import com.example.oopsLog.prompt.AiGardenerPromptBuilder;
import com.example.oopsLog.repository.AnalysisRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    private final AnalysisRepository repository;
    private final AiGardenerPromptBuilder promptBuilder;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisService(AnalysisRepository repository, AiGardenerPromptBuilder promptBuilder) {
        this.repository = repository;
        this.promptBuilder = promptBuilder;
    }

    public AnalysisResponse analyze(String text) throws Exception {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new IllegalStateException("Missing Gemini API key. Set `gemini.api-key` in application.yaml.");
        }

        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(
                                Map.of("text", systemPrompt)
                        )
                ),
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", userPrompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 4096,
                        "responseMimeType", "application/json"
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel + ":generateContent?key=" + geminiApiKey;

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getBody() == null || response.getBody().isBlank()) {
            throw new IllegalStateException("Gemini 응답 본문이 비어 있습니다.");
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        validateGeminiResponse(root);

        String rawText = extractGeminiText(root);
        String json = extractJsonObject(rawText);

        AnalysisResponse body = objectMapper.readValue(json, AnalysisResponse.class);

        AnalysisRecord record = new AnalysisRecord();
        record.setText(text);
        record.setReframe(json);

        if (body.getAnalysis() != null) {
            if (body.getAnalysis().getCoreInterpretation() != null) {
                record.setSummary(body.getAnalysis().getCoreInterpretation());
            }
            if (body.getAnalysis().getDistortions() != null) {
                record.setBiasRaw(String.join(",", body.getAnalysis().getDistortions()));
            }
            if (body.getAnalysis().getFacts() != null) {
                record.setActionsRaw(String.join("|", body.getAnalysis().getFacts()));
            }
        }

        repository.save(record);
        return body;
    }

    public List<AnalysisResponse> getHistory() {
        return repository.findAll().stream().map(record -> {
            AnalysisResponse dto;
            String json = record.getReframe();

            if (json != null && !json.isBlank()) {
                try {
                    dto = objectMapper.readValue(json, AnalysisResponse.class);
                } catch (Exception ignored) {
                    dto = new AnalysisResponse();
                }
            } else {
                dto = new AnalysisResponse();
            }

            return dto;
        }).toList();
    }

    private void validateGeminiResponse(JsonNode root) {
        JsonNode candidates = root.path("candidates");

        if (!candidates.isArray() || candidates.isEmpty()) {
            throw new IllegalStateException("Gemini 응답에 candidates가 없습니다: " + root.toPrettyString());
        }

        JsonNode candidate = candidates.get(0);
        String finishReason = candidate.path("finishReason").asText("");

        if ("MAX_TOKENS".equals(finishReason)) {
            throw new IllegalStateException("Gemini 응답이 maxOutputTokens 제한에 걸려 잘렸습니다. 프롬프트를 줄이거나 maxOutputTokens를 더 늘려야 합니다.");
        }

        if ("SAFETY".equals(finishReason)) {
            throw new IllegalStateException("Gemini 응답이 safety 정책으로 차단되었습니다.");
        }

        JsonNode parts = candidate.path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            throw new IllegalStateException("Gemini 응답 content.parts가 비어 있습니다: " + root.toPrettyString());
        }
    }

    private String extractGeminiText(JsonNode root) {
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return "";
        }

        JsonNode candidate = candidates.get(0);
        JsonNode parts = candidate.path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return "";
        }

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
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
            cleaned = cleaned.replaceFirst("\\s*```$", "");
            cleaned = cleaned.trim();
        }

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');

        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1).trim();
        }

        return cleaned;
    }
}