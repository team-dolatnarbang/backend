package com.example.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        T data,
        Meta meta
) {
    public record Meta(
            int status,
            String message
    ) {
        public static Meta of(int status, String message) {
            return new Meta(status, message);
        }
    }

    public static <T> ApiResponse<T> of(int status, String message, T data) {
        return new ApiResponse<>(data, Meta.of(status, message));
    }

    public static <T> ApiResponse<T> ok(T data) {
        return of(200, "OK", data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return of(201, "CREATED", data);
    }

    public static ApiResponse<Void> noContent() {
        return of(204, "NO_CONTENT", null);
    }
}

