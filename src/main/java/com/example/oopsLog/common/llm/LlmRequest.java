package com.example.oopsLog.common.llm;

public record LlmRequest(
        String systemPrompt,
        String userPrompt
) {
}

