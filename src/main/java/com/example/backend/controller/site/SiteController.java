package com.example.backend.controller.site;

import com.example.backend.common.error.ApiException;
import com.example.backend.common.error.ErrorCode;
import com.example.backend.common.response.ApiResponse;
import com.example.backend.dto.site.response.SiteDetailResponse;
import com.example.backend.dto.site.response.SiteSummaryResponse;
import com.example.backend.dto.site.response.SitesResponse;
import com.example.backend.service.site.SiteQueryService;
import com.example.backend.service.site.SiteService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;
    private final SiteQueryService siteQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<SitesResponse>> listSites() {
        List<SiteSummaryResponse> sites = siteService.listSites().stream()
                .map(SiteSummaryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(new SitesResponse(sites)));
    }

    @GetMapping("/{siteId}")
    public ResponseEntity<ApiResponse<SiteDetailResponse>> getSite(
            @PathVariable String siteId,
            @RequestHeader(name = "X-Session-Id", required = false) String sessionId
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

        SiteDetailResponse body = siteQueryService.getSiteDetail(parsedSiteId, parsedSessionId);
        return ResponseEntity.ok(ApiResponse.ok(body));
    }
}

