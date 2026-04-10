package com.example.oopsLog.common.auth;

import com.example.oopsLog.common.exception.CustomException;
import com.example.oopsLog.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

public class SessionAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Object userIdAttr = session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (!(userIdAttr instanceof Long loginUserId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Object pathVars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVars instanceof Map<?, ?> rawVars) {
            Object userIdVar = rawVars.get("userId");
            if (userIdVar != null) {
                Long pathUserId = parseLongOrNull(userIdVar.toString());
                if (pathUserId == null || !pathUserId.equals(loginUserId)) {
                    throw new CustomException(ErrorCode.FORBIDDEN);
                }
            }
        }

        return true;
    }

    private Long parseLongOrNull(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

