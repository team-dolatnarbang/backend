package com.example.backend.dto.site.response;

import com.example.backend.domain.site.Site;
import java.util.UUID;

public record SiteSummaryResponse(
        UUID id,
        int order,
        String name,
        String imageUrl,
        String shortDescription
) {
    public static SiteSummaryResponse from(Site site) {
        return new SiteSummaryResponse(
                site.getId(),
                site.getOrder(),
                site.getName(),
                site.getImageUrl(),
                site.getShortDescription()
        );
    }
}

