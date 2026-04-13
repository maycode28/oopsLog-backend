package com.example.oopsLog.common.llm;

public enum LlmProvider {
    GEMINI,
    OPENAI;

    public static LlmProvider fromString(String value) {
        if (value == null) throw new IllegalArgumentException("provider is null");
        return switch (value.trim().toLowerCase()) {
            case "gemini" -> GEMINI;
            case "openai" -> OPENAI;
            default -> throw new IllegalArgumentException("Unknown provider: " + value);
        };
    }
}

