package com.example.backend.controller.tribute;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.example.backend.common.response.ApiResponse;
import com.example.backend.dto.tribute.request.CreateTributeRequest;
import com.example.backend.dto.tribute.response.CreateTributeResponse;
import com.example.backend.dto.tribute.response.TributesResponse;
import com.example.backend.service.tribute.TributeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tributes")
@RequiredArgsConstructor
public class TributeController {

    private final TributeService tributeService;

    // 마지막 헌화 생성 API - 회차당 1회, 멱등키 지원
    @PostMapping
    public ResponseEntity<ApiResponse<CreateTributeResponse>> create(
            @RequestHeader(name = "X-Session-Id", required = false) String sessionId,
            @RequestBody CreateTributeRequest request
    ) {
        UUID parsedSessionId = parseSessionId(sessionId);
        CreateTributeResponse body = tributeService.create(parsedSessionId, request);
        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    // 헌화 메시지 피드 조회 API
    @GetMapping
    public ResponseEntity<ApiResponse<TributesResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        TributesResponse body = tributeService.list(page, size);
        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    private UUID parseSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        try {
            return UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }
}

