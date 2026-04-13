package com.example.oopsLog.common.llm;

public record LlmResult(
        LlmProvider provider,
        String text,
        String finishReason,
        LlmUsage usage
) {
}
