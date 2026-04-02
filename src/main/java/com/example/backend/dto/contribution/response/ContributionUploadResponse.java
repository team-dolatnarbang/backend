package com.example.backend.dto.contribution.response;

import java.util.UUID;

public record ContributionUploadResponse(
        UUID contributionId,
        String status,
        UUID siteId,
        String contributorName
) {
}

