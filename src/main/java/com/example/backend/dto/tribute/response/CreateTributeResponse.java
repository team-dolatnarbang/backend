package com.example.backend.dto.tribute.response;

import java.time.Instant;
import java.util.UUID;

public record CreateTributeResponse(
        UUID tributeId,
        int camelliaCount,
        int pledgedAmountWon,
        Instant createdAt
) {
}

