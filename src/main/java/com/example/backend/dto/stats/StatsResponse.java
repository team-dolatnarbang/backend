package com.example.backend.dto.stats;

import java.time.Instant;

public record StatsResponse(
        long participantCount,
        long camelliaTotal,
        long pledgedAmountTotalWon,
        int pledgeUnitWon,
        String currency,
        int refreshIntervalSec,
        Instant updatedAt
) {
}
