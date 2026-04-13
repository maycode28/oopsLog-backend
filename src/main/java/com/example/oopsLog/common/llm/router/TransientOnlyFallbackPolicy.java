package com.example.oopsLog.common.llm.router;

import com.example.oopsLog.common.llm.LlmProviderException;

public class TransientOnlyFallbackPolicy implements LlmFallbackPolicy {

    @Override
    public boolean shouldFallback(LlmProviderException primaryFailure) {
        return primaryFailure != null && primaryFailure.isTransient();
    }
}

