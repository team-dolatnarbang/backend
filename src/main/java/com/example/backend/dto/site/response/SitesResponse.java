package com.example.backend.dto.site.response;

import java.util.List;

public record SitesResponse(
        List<SiteSummaryResponse> sites
) {
}

