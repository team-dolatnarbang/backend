package com.example.backend.dto.site.response;

import com.example.backend.domain.site.Site;
import java.util.UUID;

public record SiteDetailResponse(
        UUID id,
        int order,
        String name,
        String narrationAudioUrl,
        int narrationDurationSec,
        boolean unlocked,
        boolean listenCompleted,
        ElderStoryResponse elderStory
) {
    public static SiteDetailResponse of(
            Site site,
            boolean unlocked,
            boolean listenCompleted,
            ElderStoryResponse elderStory
    ) {
        return new SiteDetailResponse(
                site.getId(),
                site.getOrder(),
                site.getName(),
                site.getNarrationAudioUrl(),
                site.getNarrationDurationSec(),
                unlocked,
                listenCompleted,
                elderStory
        );
    }
}

