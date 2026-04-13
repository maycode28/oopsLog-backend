package com.example.oopsLog.common.llm;

import com.example.oopsLog.common.exception.CustomException;
import com.example.oopsLog.common.exception.ErrorCode;
import com.example.oopsLog.common.llm.provider.LlmClient;
import com.example.oopsLog.common.llm.ratelimit.InMemoryFixedWindowRateLimiter;
import com.example.oopsLog.common.llm.ratelimit.RateLimitConfig;
import com.example.oopsLog.common.llm.router.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LlmOrchestratorTest {

    @Test
    void geminiSuccess_doesNotCallOpenAi() {
        StubClient gemini = StubClient.success(LlmProvider.GEMINI, "{\"ok\":true}");
        StubClient openai = StubClient.success(LlmProvider.OPENAI, "{\"ok\":true}");

        LlmOrchestrator orchestrator = orchestrator(
                List.of(gemini, openai),
                new LlmRouterConfig(LlmProvider.GEMINI, LlmProvider.OPENAI, true, 2),
                new RateLimitConfig(false, 60, 5)
        );

        LlmResult result = orchestrator.generate(new LlmRequest("s", "u"), 1L);

        assertEquals(LlmProvider.GEMINI, result.provider());
        assertEquals(1, gemini.calls.get());
        assertEquals(0, openai.calls.get());
    }

    @Test
    void geminiUnavailable_fallsBackToOpenAi_once() {
        StubClient gemini = StubClient.failure(LlmProvider.GEMINI,
                new LlmProviderException(LlmProvider.GEMINI, LlmFailureCategory.TRANSIENT, 503, "unavailable", null));
        StubClient openai = StubClient.success(LlmProvider.OPENAI, "{\"ok\":true}");

        LlmOrchestrator orchestrator = orchestrator(
                List.of(gemini, openai),
                new LlmRouterConfig(LlmProvider.GEMINI, LlmProvider.OPENAI, true, 2),
                new RateLimitConfig(false, 60, 5)
        );

        LlmResult result = orchestrator.generate(new LlmRequest("s", "u"), 1L);

        assertEquals(LlmProvider.OPENAI, result.provider());
        assertEquals(1, gemini.calls.get());
        assertEquals(1, openai.calls.get());
    }

    @Test
    void geminiAuthFailure_doesNotFallback() {
        StubClient gemini = StubClient.failure(LlmProvider.GEMINI,
                new LlmProviderException(LlmProvider.GEMINI, LlmFailureCategory.AUTH, 401, "unauthorized", null));
        StubClient openai = StubClient.success(LlmProvider.OPENAI, "{\"ok\":true}");

        LlmOrchestrator orchestrator = orchestrator(
                List.of(gemini, openai),
                new LlmRouterConfig(LlmProvider.GEMINI, LlmProvider.OPENAI, true, 2),
                new RateLimitConfig(false, 60, 5)
        );

        CustomException ex = assertThrows(CustomException.class, () -> orchestrator.generate(new LlmRequest("s", "u"), 1L));
        assertEquals(ErrorCode.LLM_AUTH_FAILED, ex.getErrorCode());
        assertEquals(1, gemini.calls.get());
        assertEquals(0, openai.calls.get());
    }

    @Test
    void maxAttempts1_neverFallsBack() {
        StubClient gemini = StubClient.failure(LlmProvider.GEMINI,
                new LlmProviderException(LlmProvider.GEMINI, LlmFailureCategory.TRANSIENT, 503, "unavailable", null));
        StubClient openai = StubClient.success(LlmProvider.OPENAI, "{\"ok\":true}");

        LlmOrchestrator orchestrator = orchestrator(
                List.of(gemini, openai),
                new LlmRouterConfig(LlmProvider.GEMINI, LlmProvider.OPENAI, true, 1),
                new RateLimitConfig(false, 60, 5)
        );

        CustomException ex = assertThrows(CustomException.class, () -> orchestrator.generate(new LlmRequest("s", "u"), 1L));
        assertEquals(ErrorCode.LLM_TEMPORARILY_UNAVAILABLE, ex.getErrorCode());
        assertEquals(1, gemini.calls.get());
        assertEquals(0, openai.calls.get());
    }

    @Test
    void rateLimit_blocksBeforeProviderCall() {
        StubClient gemini = StubClient.success(LlmProvider.GEMINI, "{\"ok\":true}");

        LlmOrchestrator orchestrator = new LlmOrchestrator(
                List.of(gemini),
                new LlmRouterConfig(LlmProvider.GEMINI, LlmProvider.OPENAI, false, 1),
                new TransientOnlyFallbackPolicy(),
                new InMemoryFixedWindowRateLimiter(),
                new RateLimitConfig(true, 60, 1),
                new LlmRequestIdResolver(),
                new LlmRateLimitKeyResolver()
        );

        orchestrator.generate(new LlmRequest("s", "u"), 99L);

        CustomException ex = assertThrows(CustomException.class, () -> orchestrator.generate(new LlmRequest("s", "u"), 99L));
        assertEquals(ErrorCode.LLM_RATE_LIMITED, ex.getErrorCode());

        assertEquals(1, gemini.calls.get());
    }

    private LlmOrchestrator orchestrator(List<LlmClient> clients, LlmRouterConfig routerConfig, RateLimitConfig rateLimitConfig) {
        return new LlmOrchestrator(
                clients,
                routerConfig,
                new TransientOnlyFallbackPolicy(),
                new InMemoryFixedWindowRateLimiter(),
                rateLimitConfig,
                new LlmRequestIdResolver(),
                new LlmRateLimitKeyResolver()
        );
    }

    static final class StubClient implements LlmClient {
        final AtomicInteger calls = new AtomicInteger();
        final LlmProvider provider;
        final RuntimeException failure;
        final String successText;

        private StubClient(LlmProvider provider, RuntimeException failure, String successText) {
            this.provider = provider;
            this.failure = failure;
            this.successText = successText;
        }

        static StubClient success(LlmProvider provider, String text) {
            return new StubClient(provider, null, text);
        }

        static StubClient failure(LlmProvider provider, RuntimeException failure) {
            return new StubClient(provider, failure, null);
        }

        @Override
        public LlmProvider provider() {
            return provider;
        }

        @Override
        public LlmResult generate(LlmRequest request, String requestId) {
            calls.incrementAndGet();
            if (failure != null) throw failure;
            return new LlmResult(provider, successText, "stop", null);
        }
    }
}
