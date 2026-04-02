package com.example.backend.service.progress;

import com.example.backend.domain.session.AnonymousSession;
import com.example.backend.dto.progress.response.ResetProgressResponse;
import com.example.backend.repository.AnonymousSessionRepository;
import com.example.backend.service.session.SessionService;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProgressService {

    private final SessionService sessionService;
    private final AnonymousSessionRepository anonymousSessionRepository;

    public ResetProgressResponse reset(UUID sessionId) {
        Objects.requireNonNull(sessionId, "sessionId");

        AnonymousSession session = sessionService.getOrCreate(sessionId);
        session.resetProgress(Instant.now());
        anonymousSessionRepository.save(session);

        return new ResetProgressResponse(session.getResetVersion());
    }
}

