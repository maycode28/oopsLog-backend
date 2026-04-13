package com.example.oopsLog.common.llm.provider;

import com.example.oopsLog.common.llm.LlmProvider;
import com.example.oopsLog.common.llm.LlmRequest;
import com.example.oopsLog.common.llm.LlmResult;

public interface LlmClient {
    LlmProvider provider();

    LlmResult generate(LlmRequest request, String requestId);
}

