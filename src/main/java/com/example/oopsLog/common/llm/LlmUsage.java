package com.example.oopsLog.common.llm;

public record LlmUsage(
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens
) {
}

