package com.example.oopsLog.common.llm.router;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class LlmRateLimitKeyResolver {

    public String resolve(Long userId, String requestId) {
        if (userId != null) {
            return "user:" + userId;
        }

        HttpServletRequest request = currentRequestOrNull();
        if (request != null) {
            HttpSession session = request.getSession(false);
            if (session != null && session.getId() != null && !session.getId().isBlank()) {
                return "session:" + session.getId();
            }
            String remoteAddr = request.getRemoteAddr();
            if (remoteAddr != null && !remoteAddr.isBlank()) {
                return "ip:" + remoteAddr;
            }
        }

        return "anonymous:" + (requestId != null ? requestId : "unknown");
    }

    private HttpServletRequest currentRequestOrNull() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }
}
