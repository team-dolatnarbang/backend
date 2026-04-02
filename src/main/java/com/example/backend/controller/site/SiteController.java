package com.example.backend.controller.site;

import com.example.backend.common.response.ApiResponse;
import com.example.backend.dto.site.response.SiteSummaryResponse;
import com.example.backend.dto.site.response.SitesResponse;
import com.example.backend.service.site.SiteService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SitesResponse>> listSites() {
        List<SiteSummaryResponse> sites = siteService.listSites().stream()
                .map(SiteSummaryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(new SitesResponse(sites)));
    }

    @GetMapping("/{siteId}")
    public ResponseEntity<ApiResponse<Object>> getSite(@PathVariable String siteId) {
        // TODO: 상세 응답 DTO + unlocked/listenCompleted/elderStory 조합 후 구현
        return ResponseEntity.ok(ApiResponse.ok(Map.of("siteId", siteId)));
    }
}

