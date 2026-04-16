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

    private final FinalTributeRepository finalTributeRepository;

    @Transactional(readOnly = true)
    public StatsResponse getPublicStats() {
        long participantCount = finalTributeRepository.count();
        long camelliaTotal = finalTributeRepository.sumCamelliaCount();
        long pledgedAmountTotalWon = finalTributeRepository.sumPledgedAmountWon();
        return new StatsResponse(
                participantCount,
                camelliaTotal,
                pledgedAmountTotalWon,
                PLEDGE_UNIT_WON,
                "KRW",
                0,
                Instant.now()
        );
    }
}
