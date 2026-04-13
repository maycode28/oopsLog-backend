package com.example.oopsLog.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    LOGIN_ID_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 로그인 아이디입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "로그인 아이디 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "분석 결과를 찾을 수 없습니다."),
    ANIMAL_NOT_FOUND(HttpStatus.NOT_FOUND, "동물을 찾을 수 없습니다."),
    LLM_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),
    LLM_TEMPORARILY_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "LLM 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해 주세요."),
    LLM_BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청을 처리할 수 없습니다."),
    LLM_AUTH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LLM 인증 설정에 문제가 있습니다."),
    LLM_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LLM 호출에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
