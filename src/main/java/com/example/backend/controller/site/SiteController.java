package com.example.backend.controller.site;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.example.backend.common.response.ApiResponse;
import com.example.backend.dto.site.request.CompleteListenRequest;
import com.example.backend.dto.site.response.CompleteListenResponse;
import com.example.backend.dto.site.response.SiteDetailResponse;
import com.example.backend.dto.site.response.SiteSummaryResponse;
import com.example.backend.dto.site.response.SitesResponse;
import com.example.backend.service.progress.ListenCompletionService;
import com.example.backend.service.site.SiteQueryService;
import com.example.backend.service.site.SiteService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;
    private final SiteQueryService siteQueryService;
    private final ListenCompletionService listenCompletionService;

    // 유적지 목록 조회 API
    @GetMapping
    public ResponseEntity<ApiResponse<SitesResponse>> listSites() {
        List<SiteSummaryResponse> sites = siteService.listSites().stream()
                .map(SiteSummaryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(new SitesResponse(sites)));
    }

    // 유적지 상세 조회 API
    @GetMapping("/{siteId}")
    public ResponseEntity<ApiResponse<SiteDetailResponse>> getSite(@PathVariable String siteId) {
        UUID parsedSiteId;
        try {
            parsedSiteId = UUID.fromString(siteId);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        SiteDetailResponse body = siteQueryService.getSiteDetail(parsedSiteId);
        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    @PostMapping("/{siteId}/complete-listen")
    public ResponseEntity<ApiResponse<CompleteListenResponse>> completeListen(
            @PathVariable String siteId,
            @RequestHeader(name = "X-Session-Id", required = false) String sessionId,
            @RequestBody(required = false) CompleteListenRequest request
    ) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        UUID parsedSiteId;
        UUID parsedSessionId;
        try {
            parsedSiteId = UUID.fromString(siteId);
            parsedSessionId = UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        Integer duration = request != null ? request.durationListenedSec() : null;
        CompleteListenResponse body = listenCompletionService.completeListen(parsedSessionId, parsedSiteId, duration);
        return ResponseEntity.ok(ApiResponse.ok(body));
    }
}

