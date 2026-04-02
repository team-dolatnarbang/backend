package com.example.backend.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        ErrorBody error
) {
    public record ErrorBody(
            String code,
            String message,
            Map<String, Object> details
    ) {
        public static ErrorBody of(String code, String message, Map<String, Object> details) {
            return new ErrorBody(code, message, details);
        }
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(ErrorBody.of(code, message, Map.of()));
    }

    public static ErrorResponse of(String code, String message, Map<String, Object> details) {
        return new ErrorResponse(ErrorBody.of(code, message, details));
    }
}

