package com.example.backend.controller.progress;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.example.backend.common.response.ApiResponse;
import com.example.backend.dto.progress.response.ResetProgressResponse;
import com.example.backend.service.progress.ProgressService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // 진행 리셋 API (POST /progress/reset) - resetVersion 증가 후 반환
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<ResetProgressResponse>> reset(
            @RequestHeader(name = "X-Session-Id", required = false) String sessionId
    ) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        UUID parsedSessionId;
        try {
            parsedSessionId = UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        ResetProgressResponse body = progressService.reset(parsedSessionId);
        return ResponseEntity.ok(ApiResponse.ok(body));
    }
}

