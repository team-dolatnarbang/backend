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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/tributes", "/reviews"})
@RequiredArgsConstructor
public class TributeController {

    private final TributeService tributeService;

    // 마지막 플로우 후기(헌화) 생성 — 회차당 1회, 멱등키 지원. `/reviews` 는 동일 동작의 별칭.
    @PostMapping
    public ResponseEntity<ApiResponse<CreateTributeResponse>> create(
            @RequestHeader(name = "X-Session-Id", required = false) String sessionId,
            @RequestBody CreateTributeRequest request
    ) {
        UUID parsedSessionId = parseSessionId(sessionId);
        CreateTributeResponse body = tributeService.create(parsedSessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(body));
    }

    // 후기(헌화) 공개 목록
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

