package com.example.oopsLog.common.llm.router;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

public class LlmRequestIdResolver {

    public String resolve() {
        HttpServletRequest request = currentRequestOrNull();
        if (request == null) return UUID.randomUUID().toString();

        String header = firstNonBlank(
                request.getHeader("X-Request-Id"),
                request.getHeader("X-Correlation-Id")
        );
        return header != null ? header : UUID.randomUUID().toString();
    }

    private HttpServletRequest currentRequestOrNull() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) return v.trim();
        }
        return null;
    }
}

