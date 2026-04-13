package com.example.oopsLog.common.llm;

public class LlmProviderException extends RuntimeException {

    private final LlmProvider provider;
    private final LlmFailureCategory category;
    private final Integer httpStatus;

    public LlmProviderException(LlmProvider provider, LlmFailureCategory category, Integer httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.category = category;
        this.httpStatus = httpStatus;
    }

    public LlmProvider getProvider() {
        return provider;
    }

    public LlmFailureCategory getCategory() {
        return category;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public boolean isTransient() {
        return category == LlmFailureCategory.TRANSIENT;
    }
}

