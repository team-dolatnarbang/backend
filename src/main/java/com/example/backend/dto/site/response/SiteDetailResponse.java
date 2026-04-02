package com.example.backend.dto.site.response;

import com.example.backend.domain.site.Site;
import java.util.UUID;

public record SiteDetailResponse(
        UUID id,
        int order,
        ElderStoryResponse elderStory
) {
    public static SiteDetailResponse of(Site site, ElderStoryResponse elderStory) {
        return new SiteDetailResponse(site.getId(), site.getOrder(), elderStory);
    }
}

