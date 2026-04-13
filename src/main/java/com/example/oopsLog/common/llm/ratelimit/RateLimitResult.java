package com.example.oopsLog.common.llm.ratelimit;

public record RateLimitResult(
        boolean allowed,
        long currentCount,
        long maxRequests,
        long windowSeconds
) {
}

