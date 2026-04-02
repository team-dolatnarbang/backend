package com.example.backend.service.stats;

import com.example.backend.dto.stats.StatsResponse;
import com.example.backend.repository.FinalTributeRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatsService {

    private static final int PLEDGE_UNIT_WON = 1000;
    private static final int REFRESH_INTERVAL_SEC = 600;

    private final FinalTributeRepository finalTributeRepository;

    private volatile CachedStats cache;

    @Transactional(readOnly = true)
    public StatsResponse getPublicStats() {
        long now = System.currentTimeMillis();
        CachedStats c = cache;
        if (c != null && now < c.expiresAtEpochMs) {
            return c.stats;
        }
        return refreshLocked(now);
    }

    private synchronized StatsResponse refreshLocked(long now) {
        CachedStats c = cache;
        if (c != null && now < c.expiresAtEpochMs) {
            return c.stats;
        }
        long participantCount = finalTributeRepository.count();
        long camelliaTotal = finalTributeRepository.sumCamelliaCount();
        long pledgedAmountTotalWon = finalTributeRepository.sumPledgedAmountWon();
        Instant updatedAt = Instant.now();
        StatsResponse stats = new StatsResponse(
                participantCount,
                camelliaTotal,
                pledgedAmountTotalWon,
                PLEDGE_UNIT_WON,
                "KRW",
                REFRESH_INTERVAL_SEC,
                updatedAt
        );
        cache = new CachedStats(stats, now + REFRESH_INTERVAL_SEC * 1000L);
        return stats;
    }

    private record CachedStats(StatsResponse stats, long expiresAtEpochMs) {}
}
