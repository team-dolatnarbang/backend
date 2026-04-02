package com.example.backend.dto.site.response;

import java.util.UUID;

public record CompleteListenResponse(
        UUID siteId,
        boolean listenCompleted,
        UUID nextSiteId
) {
    public static CompleteListenResponse completed(UUID siteId, UUID nextSiteId) {
        return new CompleteListenResponse(siteId, true, nextSiteId);
    }
}

