package com.example.backend.repository;

import com.example.backend.domain.progress.ListenCompletion;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListenCompletionRepository extends JpaRepository<ListenCompletion, UUID> {

    // 특정 세션의 특정 회차에서 해당 유적지를 이미 완료했는지(=listenCompleted) 확인할 때 사용
    boolean existsBySession_SessionIdAndResetVersionAndSite_Id(UUID sessionId, int resetVersion, UUID siteId);

    // 특정 세션의 특정 회차에서 완료한 유적지(핑) 개수 집계(예: 마지막 헌화 조건 판단)할 때 사용
    long countBySession_SessionIdAndResetVersion(UUID sessionId, int resetVersion);
}

