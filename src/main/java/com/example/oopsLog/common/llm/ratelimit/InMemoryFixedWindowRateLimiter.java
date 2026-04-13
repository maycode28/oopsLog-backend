package com.example.oopsLog.common.llm.ratelimit;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryFixedWindowRateLimiter implements RateLimiter {

    private static final class Counter {
        private final long windowStartEpochSecond;
        private final long count;

        private Counter(long windowStartEpochSecond, long count) {
            this.windowStartEpochSecond = windowStartEpochSecond;
            this.count = count;
        }
    }

    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    @Override
    public RateLimitResult tryAcquire(String key, long windowSeconds, long maxRequests) {
        if (windowSeconds <= 0 || maxRequests <= 0) {
            return new RateLimitResult(true, 0, maxRequests, windowSeconds);
        }

        long now = Instant.now().getEpochSecond();
        long windowStart = now - (now % windowSeconds);

        Counter updated = counters.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStartEpochSecond != windowStart) {
                return new Counter(windowStart, 1);
            }
            return new Counter(existing.windowStartEpochSecond, existing.count + 1);
        });

        // best-effort cleanup to avoid unbounded growth
        counters.entrySet().removeIf(e -> now - e.getValue().windowStartEpochSecond > windowSeconds * 2);

        boolean allowed = updated.count <= maxRequests;
        return new RateLimitResult(allowed, updated.count, maxRequests, windowSeconds);
    }
}

