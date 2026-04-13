package com.example.oopsLog.config;

import com.example.oopsLog.common.llm.LlmProvider;
import com.example.oopsLog.common.llm.provider.GeminiLlmClient;
import com.example.oopsLog.common.llm.provider.LlmClient;
import com.example.oopsLog.common.llm.provider.OpenAiLlmClient;
import com.example.oopsLog.common.llm.ratelimit.InMemoryFixedWindowRateLimiter;
import com.example.oopsLog.common.llm.ratelimit.RateLimitConfig;
import com.example.oopsLog.common.llm.ratelimit.RateLimiter;
import com.example.oopsLog.common.llm.router.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LlmConfig {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public LlmClient geminiLlmClient(
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model,
            @Value("${gemini.timeout.connect-ms:5000}") int connectTimeoutMs,
            @Value("${gemini.timeout.read-ms:30000}") int readTimeoutMs,
            ObjectMapper objectMapper
    ) {
        return new GeminiLlmClient(apiKey, model, restTemplate(connectTimeoutMs, readTimeoutMs), objectMapper);
    }

    @Bean
    public LlmClient openAiLlmClient(
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-5.4-mini}") String model,
            @Value("${openai.timeout.connect-ms:5000}") int connectTimeoutMs,
            @Value("${openai.timeout.read-ms:30000}") int readTimeoutMs,
            ObjectMapper objectMapper
    ) {
        return new OpenAiLlmClient(apiKey, model, restTemplate(connectTimeoutMs, readTimeoutMs), objectMapper);
    }

    @Bean
    public RateLimiter llmRateLimiter() {
        return new InMemoryFixedWindowRateLimiter();
    }

    @Bean
    public RateLimitConfig rateLimitConfig(
            @Value("${llm.rate-limit.enabled:true}") boolean enabled,
            @Value("${llm.rate-limit.window-seconds:60}") long windowSeconds,
            @Value("${llm.rate-limit.max-requests:5}") long maxRequests
    ) {
        return new RateLimitConfig(enabled, windowSeconds, maxRequests);
    }

    @Bean
    public LlmRouterConfig llmRouterConfig(
            @Value("${llm.primary:gemini}") String primary,
            @Value("${llm.fallback:openai}") String fallback,
            @Value("${llm.fallback-enabled:true}") boolean fallbackEnabled,
            @Value("${llm.max-provider-attempts-per-request:2}") int maxAttempts
    ) {
        return new LlmRouterConfig(
                LlmProvider.fromString(primary),
                LlmProvider.fromString(fallback),
                fallbackEnabled,
                maxAttempts
        );
    }

    @Bean
    public LlmFallbackPolicy llmFallbackPolicy() {
        return new TransientOnlyFallbackPolicy();
    }

    @Bean
    public LlmRequestIdResolver llmRequestIdResolver() {
        return new LlmRequestIdResolver();
    }

    @Bean
    public LlmRateLimitKeyResolver llmRateLimitKeyResolver() {
        return new LlmRateLimitKeyResolver();
    }

    @Bean
    public LlmOrchestrator llmOrchestrator(
            java.util.List<LlmClient> clients,
            LlmRouterConfig routerConfig,
            LlmFallbackPolicy fallbackPolicy,
            RateLimiter rateLimiter,
            RateLimitConfig rateLimitConfig,
            LlmRequestIdResolver requestIdResolver,
            LlmRateLimitKeyResolver keyResolver
    ) {
        return new LlmOrchestrator(
                clients,
                routerConfig,
                fallbackPolicy,
                rateLimiter,
                rateLimitConfig,
                requestIdResolver,
                keyResolver
        );
    }

    private RestTemplate restTemplate(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }
}
