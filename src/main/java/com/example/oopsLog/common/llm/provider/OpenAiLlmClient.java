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

public class OpenAiLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiLlmClient.class);

    private final String apiKey;
    private final String model;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAiLlmClient(String apiKey, String model, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmProvider provider() {
        return LlmProvider.OPENAI;
    }

    @Override
    public LlmResult generate(LlmRequest request, String requestId) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new LlmProviderException(provider(), LlmFailureCategory.AUTH, null, "Missing OpenAI API key", null);
        }
        if (model == null || model.isBlank()) {
            throw new LlmProviderException(provider(), LlmFailureCategory.BAD_REQUEST, null, "Missing OpenAI model", null);
        }

        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.add("X-Request-Id", requestId);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", request.systemPrompt()),
                        Map.of("role", "user", "content", request.userPrompt())
                ),
                "temperature", 0.2,
                "max_tokens", 4096,
                "response_format", Map.of("type", "json_object")
        );

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(requestBody, headers), String.class);

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new LlmProviderException(provider(), LlmFailureCategory.UNKNOWN, response.getStatusCode().value(),
                        "OpenAI empty response body", null);
            }

            JsonNode root = objectMapper.readTree(body);
            String text = extractChatCompletionText(root);
            if (text == null || text.isBlank()) {
                throw new LlmProviderException(provider(), LlmFailureCategory.UNKNOWN, response.getStatusCode().value(),
                        "OpenAI response missing content", null);
            }
            String finishReason = extractFinishReason(root);
            LlmUsage usage = extractUsage(root);
            return new LlmResult(provider(), text.trim(), finishReason, usage);
        } catch (HttpStatusCodeException e) {
            int status = e.getStatusCode().value();
            LlmFailureCategory category = classifyHttpStatus(status);
            log.debug("OpenAI error requestId={} status={} body={}", requestId, status, safeBody(e));
            throw new LlmProviderException(provider(), category, status, "OpenAI HTTP error: " + status, e);
        } catch (ResourceAccessException e) {
            throw new LlmProviderException(provider(), LlmFailureCategory.TRANSIENT, null, "OpenAI network error", e);
        } catch (Exception e) {
            throw new LlmProviderException(provider(), LlmFailureCategory.UNKNOWN, null, "OpenAI unexpected error", e);
        }
    }

    private String extractChatCompletionText(JsonNode root) {
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) return null;
        return choices.get(0).path("message").path("content").asText(null);
    }

    private String extractFinishReason(JsonNode root) {
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) return null;
        return choices.get(0).path("finish_reason").asText(null);
    }

    private LlmUsage extractUsage(JsonNode root) {
        JsonNode usage = root.path("usage");
        if (usage.isMissingNode() || usage.isNull()) return null;
        Integer input = usage.has("prompt_tokens") ? usage.path("prompt_tokens").asInt() : null;
        Integer output = usage.has("completion_tokens") ? usage.path("completion_tokens").asInt() : null;
        Integer total = usage.has("total_tokens") ? usage.path("total_tokens").asInt() : null;
        if (input == null && output == null && total == null) return null;
        return new LlmUsage(input, output, total);
    }

    private LlmFailureCategory classifyHttpStatus(int status) {
        if (status >= 500) return LlmFailureCategory.TRANSIENT;
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
