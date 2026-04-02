package com.example.backend.dto.site.response;

import com.example.backend.domain.site.Site;
import java.util.UUID;

public record SiteSummaryResponse(
        UUID id,
        int order
) {
    public static SiteSummaryResponse from(Site site) {
        return new SiteSummaryResponse(site.getId(), site.getOrder());
    }
}

