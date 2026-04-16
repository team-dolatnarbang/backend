package com.example.backend.dto.tribute.response;

import com.example.backend.domain.tribute.FinalTribute;
import java.time.Instant;
import java.util.UUID;

public record TributeFeedItemResponse(
        UUID id,
        String siteName,
        String message,
        String nickname,
        int camelliaCount,
        Instant createdAt
) {
    public static TributeFeedItemResponse from(FinalTribute tribute) {
        return new TributeFeedItemResponse(
                tribute.getId(),
                "",
                tribute.getMessage(),
                tribute.getNickname(),
                tribute.getCamelliaCount(),
                tribute.getCreatedAt()
        );
    }
}

