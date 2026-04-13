package com.example.oopsLog.common.llm.ratelimit;

public interface RateLimiter {
    RateLimitResult tryAcquire(String key, long windowSeconds, long maxRequests);
}

