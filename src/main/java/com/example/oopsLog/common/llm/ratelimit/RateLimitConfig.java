package com.example.oopsLog.common.llm.ratelimit;

public record RateLimitConfig(
        boolean enabled,
        long windowSeconds,
        long maxRequests
) {
}

