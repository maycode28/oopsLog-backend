package com.example.oopsLog.common.llm.router;

import com.example.oopsLog.common.exception.CustomException;
import com.example.oopsLog.common.exception.ErrorCode;
import com.example.oopsLog.common.llm.LlmFailureCategory;
import com.example.oopsLog.common.llm.LlmProvider;
import com.example.oopsLog.common.llm.LlmProviderException;
import com.example.oopsLog.common.llm.LlmRequest;
import com.example.oopsLog.common.llm.LlmResult;
import com.example.oopsLog.common.llm.provider.LlmClient;
import com.example.oopsLog.common.llm.ratelimit.RateLimitConfig;
import com.example.oopsLog.common.llm.ratelimit.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class LlmOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(LlmOrchestrator.class);

    private final Map<LlmProvider, LlmClient> clients;
    private final LlmRouterConfig routerConfig;
    private final LlmFallbackPolicy fallbackPolicy;
    private final RateLimiter rateLimiter;
    private final RateLimitConfig rateLimitConfig;
    private final LlmRequestIdResolver requestIdResolver;
    private final LlmRateLimitKeyResolver rateLimitKeyResolver;

    public LlmOrchestrator(
            List<LlmClient> clients,
            LlmRouterConfig routerConfig,
            LlmFallbackPolicy fallbackPolicy,
            RateLimiter rateLimiter,
            RateLimitConfig rateLimitConfig,
            LlmRequestIdResolver requestIdResolver,
            LlmRateLimitKeyResolver rateLimitKeyResolver
    ) {
        this.clients = toMap(clients);
        this.routerConfig = routerConfig;
        this.fallbackPolicy = fallbackPolicy;
        this.rateLimiter = rateLimiter;
        this.rateLimitConfig = rateLimitConfig;
        this.requestIdResolver = requestIdResolver;
        this.rateLimitKeyResolver = rateLimitKeyResolver;
    }

    public LlmResult generate(LlmRequest request, Long userId) {
        String requestId = requestIdResolver.resolve();
        String rateLimitKey = rateLimitKeyResolver.resolve(userId, requestId);

        enforceRateLimit(rateLimitKey, requestId, userId);
        return generateWithFallback(request, requestId, userId);
    }

    private LlmResult generateWithFallback(LlmRequest request, String requestId, Long userId) {
        int maxAttempts = Math.max(1, routerConfig.maxProviderAttemptsPerRequest());
        LlmProvider primary = routerConfig.primary();
        LlmProvider fallback = routerConfig.fallback();

        int attempts = 0;
        try {
            attempts++;
            LlmResult primaryResult = client(primary).generate(request, requestId);
            log.info("LLM success requestId={} provider={} attempts={}/{}", requestId, primary, attempts, maxAttempts);
            return primaryResult;
        } catch (LlmProviderException primaryFailure) {
            log.warn("LLM primary failed requestId={} provider={} category={} httpStatus={} userId={}",
                    requestId, primary, primaryFailure.getCategory(), primaryFailure.getHttpStatus(), userId, primaryFailure);

            boolean canFallback = routerConfig.fallbackEnabled()
                    && attempts < maxAttempts
                    && fallbackPolicy.shouldFallback(primaryFailure);

            if (!canFallback) {
                throw wrap(primaryFailure, requestId);
            }

            log.info("LLM fallback triggered requestId={} primary={} fallback={} reasonCategory={} httpStatus={}",
                    requestId, primary, fallback, primaryFailure.getCategory(), primaryFailure.getHttpStatus());

            attempts++;
            try {
                LlmResult fallbackResult = client(fallback).generate(request, requestId);
                log.info("LLM fallback success requestId={} provider={} attempts={}/{}", requestId, fallback, attempts, maxAttempts);
                return fallbackResult;
            } catch (LlmProviderException fallbackFailure) {
                log.warn("LLM fallback failed requestId={} provider={} category={} httpStatus={} userId={}",
                        requestId, fallback, fallbackFailure.getCategory(), fallbackFailure.getHttpStatus(), userId, fallbackFailure);
                throw wrap(fallbackFailure, requestId);
            }
        }
    }

    private void enforceRateLimit(String key, String requestId, Long userId) {
        if (!rateLimitConfig.enabled()) return;

        var result = rateLimiter.tryAcquire(key, rateLimitConfig.windowSeconds(), rateLimitConfig.maxRequests());
        if (result.allowed()) return;

        log.warn("LLM rate limited requestId={} userId={} key={} count={}/{} windowSeconds={}",
                requestId, userId, key, result.currentCount(), result.maxRequests(), result.windowSeconds());
        throw new CustomException(ErrorCode.LLM_RATE_LIMITED);
    }

    private LlmClient client(LlmProvider provider) {
        LlmClient client = clients.get(provider);
        if (client == null) {
            throw new IllegalStateException("No LLM client registered for provider: " + provider);
        }
        return client;
    }

    private RuntimeException wrap(LlmProviderException e, String requestId) {
        ErrorCode errorCode = switch (e.getCategory()) {
            case TRANSIENT -> ErrorCode.LLM_TEMPORARILY_UNAVAILABLE;
            case AUTH -> ErrorCode.LLM_AUTH_FAILED;
            case BAD_REQUEST, POLICY_OR_VALIDATION, CLIENT_ERROR -> ErrorCode.LLM_BAD_REQUEST;
            case UNKNOWN -> ErrorCode.LLM_FAILED;
        };
        log.error("LLM final failure requestId={} provider={} category={} httpStatus={}",
                requestId, e.getProvider(), e.getCategory(), e.getHttpStatus(), e);
        return new CustomException(errorCode);
    }

    private Map<LlmProvider, LlmClient> toMap(List<LlmClient> clients) {
        Map<LlmProvider, LlmClient> map = new EnumMap<>(LlmProvider.class);
        for (LlmClient client : clients) {
            map.put(client.provider(), client);
        }
        return map;
    }
}

