package com.example.backend.common.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse body = ErrorResponse.of(errorCode.code(), errorCode.message(), e.getDetails());
        return ResponseEntity.status(errorCode.httpStatus().value()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        ErrorResponse body = ErrorResponse.of(errorCode.code(), errorCode.message());
        return ResponseEntity.status(errorCode.httpStatus().value()).body(body);
    }
}

