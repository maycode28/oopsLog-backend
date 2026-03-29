package com.example.oopsLog.service;

import com.example.oopsLog.dto.response.AnalysisResponse;
import com.example.oopsLog.repository.AnalysisRepository;
import com.example.oopsLog.entity.AnalysisRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final AnalysisRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisService(AnalysisRepository repository) {
        this.repository = repository;
    }

    public AnalysisResponse analyze(String text) throws Exception {
        // 1. OpenAI에 보낼 프롬프트 구성
        String prompt = """
                당신은 심리 상담 전문가입니다. 아래 실패 경험을 분석하고,
                반드시 다음 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
                
                {
                  "summary": "실패 경험 한 줄 요약",
                  "emotion": "주요 감정들 (예: 수치심, 좌절감)",
                  "bias": ["인지 왜곡 1", "인지 왜곡 2"],
                  "reframe": "새로운 시각으로 재해석한 내용",
                  "actions": ["실천 방안 1", "실천 방안 2", "실천 방안 3"]
                }
                
                분석할 실패 경험:
                """ + text;

        // 2. OpenAI API 요청 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // 3. OpenAI API 호출
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                request,
                String.class
        );

        // 4. 응답 파싱
        JsonNode root = objectMapper.readTree(response.getBody());
        String content = root
                .path("choices").get(0)
                .path("message")
                .path("content")
                .asText();

        JsonNode analysisJson = objectMapper.readTree(content);

        String summary  = analysisJson.path("summary").asText();
        String emotion  = analysisJson.path("emotion").asText();
        String reframe  = analysisJson.path("reframe").asText();

        // bias 배열 → 콤마 구분 문자열
        List<String> biasList = List.of();
        if (analysisJson.has("bias")) {
            biasList = objectMapper.convertValue(
                    analysisJson.path("bias"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        // actions 배열 → 파이프 구분 문자열
        List<String> actionsList = List.of();
        if (analysisJson.has("actions")) {
            actionsList = objectMapper.convertValue(
                    analysisJson.path("actions"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }

        // 5. DB 저장
        AnalysisRecord record = new AnalysisRecord();
        record.setText(text);
        record.setSummary(summary);
        record.setEmotion(emotion);
        record.setBiasRaw(String.join(",", biasList));
        record.setReframe(reframe);
        record.setActionsRaw(String.join("|", actionsList));

        AnalysisRecord saved = repository.save(record);

        // 6. 응답 DTO 구성
        AnalysisResponse dto = new AnalysisResponse();
        dto.setId(saved.getId());
        dto.setText(saved.getText());
        dto.setSummary(saved.getSummary());
        dto.setEmotion(saved.getEmotion());
        dto.setBias(biasList);
        dto.setReframe(saved.getReframe());
        dto.setActions(actionsList);

        return dto;
    }

    public List<AnalysisResponse> getHistory() {
        return repository.findAll().stream().map(record -> {
            AnalysisResponse dto = new AnalysisResponse();
            dto.setId(record.getId());
            dto.setText(record.getText());
            dto.setSummary(record.getSummary());
            dto.setEmotion(record.getEmotion());
            dto.setBias(record.getBias());
            dto.setReframe(record.getReframe());
            dto.setActions(record.getActions());
            return dto;
        }).toList();
    }
}
