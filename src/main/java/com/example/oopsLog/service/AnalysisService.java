package com.example.oopsLog.service;

import com.example.oopsLog.dto.response.AnalysisResponse;
import com.example.oopsLog.prompt.AiGardenerPromptBuilder;
import com.example.oopsLog.repository.AnalysisRepository;
import com.example.oopsLog.entity.AnalysisRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            throw new IllegalStateException("Missing Gemini API key. Set `gemini.api-key` in application.yaml or `GEMINI_API_KEY` env var.");
        }

        // 1) Gemini에 보낼 Cognitive-only 프롬프트 구성
        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(text);

        // 2) Gemini API 요청 구성
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
                        "maxOutputTokens", 1024
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // 3) Gemini API 호출
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel + ":generateContent?key=" + geminiApiKey;

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        // 4) 응답 파싱
        JsonNode root = objectMapper.readTree(response.getBody());
        String rawText = extractGeminiText(root);

        String json = extractJsonObject(rawText);
        AnalysisResponse body = objectMapper.readValue(json, AnalysisResponse.class);

        // 5) DB 저장
        AnalysisRecord record = new AnalysisRecord();
        record.setText(text);
        // 히스토리 조회를 위해 LLM의 raw JSON을 그대로 저장합니다.
        record.setReframe(json);
        // (선택) legacy 컬럼에 distortion/facts 일부를 채워둘 수 있습니다.
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

        AnalysisRecord saved = repository.save(record);

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
                    // 과거 데이터가 JSON 포맷이 아닌 경우를 대비합니다.
                    dto = new AnalysisResponse();
                }
            } else {
                dto = new AnalysisResponse();
            }

            return dto;
        }).toList();
    }

    private String extractGeminiText(JsonNode root) {
        // Gemini generateContent 응답에서 candidates[0].content.parts[0].text 추출
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) return "";
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) return "";
        return parts.get(0).path("text").asText("");
    }

    private String extractJsonObject(String content) {
        if (content == null) return "{}";
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }
}
