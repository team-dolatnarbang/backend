package com.example.backend.repository;

import com.example.backend.domain.progress.ListenCompletion;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListenCompletionRepository extends JpaRepository<ListenCompletion, UUID> {

    long countBySession_SessionIdAndResetVersion(UUID sessionId, int resetVersion);

    boolean existsBySession_SessionIdAndSite_IdAndResetVersion(UUID sessionId, UUID siteId, int resetVersion);

    Optional<ListenCompletion> findBySession_SessionIdAndSite_IdAndResetVersion(
            UUID sessionId, UUID siteId, int resetVersion);
}
