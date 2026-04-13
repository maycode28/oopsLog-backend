package com.example.oopsLog.common.llm;

public enum LlmFailureCategory {
    TRANSIENT,
    AUTH,
    BAD_REQUEST,
    POLICY_OR_VALIDATION,
    CLIENT_ERROR,
    UNKNOWN
}

