package com.example.backend.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "요청 값이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "리소스를 찾을 수 없습니다."),
    SITE_LOCKED(HttpStatus.FORBIDDEN, "SITE_LOCKED", "이전 유적지 청취를 완료해야 합니다."),
    TRIBUTE_NOT_ALLOWED_YET(HttpStatus.FORBIDDEN, "TRIBUTE_NOT_ALLOWED_YET", "아직 모든 핑을 완료하지 않아 헌화를 할 수 없습니다."),
    IDENTITY_CONFLICT(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", "중복 요청입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}

