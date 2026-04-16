package com.example.backend.controller.stats;

import com.example.backend.common.response.ApiResponse;
import com.example.backend.dto.stats.StatsResponse;
import com.example.backend.service.stats.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<ApiResponse<StatsResponse>> getStats() {
        StatsResponse body = statsService.getPublicStats();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.ok(body));
    }
}
