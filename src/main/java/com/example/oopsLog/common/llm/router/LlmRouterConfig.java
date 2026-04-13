package com.example.oopsLog.common.llm.router;

import com.example.oopsLog.common.llm.LlmProvider;

public record LlmRouterConfig(
        LlmProvider primary,
        LlmProvider fallback,
        boolean fallbackEnabled,
        int maxProviderAttemptsPerRequest
) {
}

