package com.example.oopsLog.common.llm.provider;

import com.example.oopsLog.common.llm.LlmFailureCategory;
import com.example.oopsLog.common.llm.LlmProvider;
import com.example.oopsLog.common.llm.LlmProviderException;
import com.example.oopsLog.common.llm.LlmRequest;
import com.example.oopsLog.common.llm.LlmResult;
import com.example.oopsLog.common.llm.LlmUsage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class GeminiLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiLlmClient.class);

    private final String apiKey;
    private final String model;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiLlmClient(String apiKey, String model, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmProvider provider() {
        return LlmProvider.GEMINI;
    }

    @Override
    public LlmResult generate(LlmRequest request, String requestId) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new LlmProviderException(provider(), LlmFailureCategory.AUTH, null, "Missing Gemini API key", null);
        }
        if (model == null || model.isBlank()) {
            throw new LlmProviderException(provider(), LlmFailureCategory.BAD_REQUEST, null, "Missing Gemini model", null);
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", request.systemPrompt()))
                ),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", request.userPrompt()))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 4096,
                        "responseMimeType", "application/json"
                )
        );

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(requestBody, headers), String.class);

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new LlmProviderException(provider(), LlmFailureCategory.UNKNOWN, response.getStatusCode().value(),
                        "Gemini empty response body", null);
            }

            JsonNode root = objectMapper.readTree(body);
            String text = extractGeminiText(root);
            if (text == null || text.isBlank()) {
                throw new LlmProviderException(provider(), LlmFailureCategory.UNKNOWN, response.getStatusCode().value(),
                        "Gemini response missing text", null);
            }
            String finishReason = extractFinishReason(root);
            LlmUsage usage = extractUsage(root);
            return new LlmResult(provider(), text.trim(), finishReason, usage);
        } catch (HttpStatusCodeException e) {
            int status = e.getStatusCode().value();
            LlmFailureCategory category = classifyHttpStatus(status);
            log.debug("Gemini error requestId={} status={} body={}", requestId, status, safeBody(e));
            throw new LlmProviderException(provider(), category, status, "Gemini HTTP error: " + status, e);
        } catch (ResourceAccessException e) {
            // timeouts / connection errors
            throw new LlmProviderException(provider(), LlmFailureCategory.TRANSIENT, null, "Gemini network error", e);
        } catch (Exception e) {
            throw new LlmProviderException(provider(), LlmFailureCategory.UNKNOWN, null, "Gemini unexpected error", e);
        }
    }

    private String extractGeminiText(JsonNode root) {
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) return null;
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (JsonNode part : parts) {
            if (part.has("text")) sb.append(part.path("text").asText(""));
        }
        return sb.toString();
    }

    private String extractFinishReason(JsonNode root) {
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) return null;
        return candidates.get(0).path("finishReason").asText(null);
    }

    private LlmUsage extractUsage(JsonNode root) {
        JsonNode usage = root.path("usageMetadata");
        if (usage.isMissingNode() || usage.isNull()) return null;
        Integer input = usage.has("promptTokenCount") ? usage.path("promptTokenCount").asInt() : null;
        Integer output = usage.has("candidatesTokenCount") ? usage.path("candidatesTokenCount").asInt() : null;
        Integer total = usage.has("totalTokenCount") ? usage.path("totalTokenCount").asInt() : null;
        if (input == null && output == null && total == null) return null;
        return new LlmUsage(input, output, total);
    }

    private LlmFailureCategory classifyHttpStatus(int status) {
        if (status >= 500) return LlmFailureCategory.TRANSIENT; // includes 503 UNAVAILABLE
        if (status == 401 || status == 403) return LlmFailureCategory.AUTH;
        if (status == 400) return LlmFailureCategory.BAD_REQUEST;
        if (status == 422) return LlmFailureCategory.POLICY_OR_VALIDATION;
        if (status >= 400) return LlmFailureCategory.CLIENT_ERROR;
        return LlmFailureCategory.UNKNOWN;
    }

    private String safeBody(HttpStatusCodeException e) {
        try {
            String body = e.getResponseBodyAsString();
            return body != null && body.length() > 2000 ? body.substring(0, 2000) : body;
        } catch (Exception ignored) {
            return null;
        }
    }
}
