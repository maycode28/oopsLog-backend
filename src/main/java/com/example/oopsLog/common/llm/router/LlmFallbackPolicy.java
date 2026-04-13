package com.example.oopsLog.common.llm.router;

import com.example.oopsLog.common.llm.LlmProviderException;

public interface LlmFallbackPolicy {
    boolean shouldFallback(LlmProviderException primaryFailure);
}

