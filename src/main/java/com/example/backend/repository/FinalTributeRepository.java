package com.example.backend.repository;

import com.example.backend.domain.tribute.FinalTribute;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FinalTributeRepository extends JpaRepository<FinalTribute, UUID> {

    // 회차당 마지막 헌화가 이미 생성되었는지 확인할 때 사용
    Optional<FinalTribute> findBySession_SessionIdAndResetVersion(UUID sessionId, int resetVersion);

    // 같은 idempotencyKey로 재요청이 들어왔을 때 기존 결과를 반환하기 위해 사용
    Optional<FinalTribute> findByIdempotencyKey(UUID idempotencyKey);

    @Query("SELECT COALESCE(SUM(f.camelliaCount), 0L) FROM FinalTribute f")
    Long sumCamelliaCount();

    @Query("SELECT COALESCE(SUM(f.pledgedAmountWon), 0L) FROM FinalTribute f")
    Long sumPledgedAmountWon();
}

