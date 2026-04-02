package com.example.backend.service.session;

import com.example.backend.domain.session.AnonymousSession;
import com.example.backend.repository.AnonymousSessionRepository;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionService {

    private final AnonymousSessionRepository anonymousSessionRepository;

    public AnonymousSession getOrCreate(UUID sessionId) {
        Objects.requireNonNull(sessionId, "sessionId");

        Instant now = Instant.now();
        return anonymousSessionRepository.findById(sessionId)
                .map(session -> {
                    session.touch(now);
                    return session;
                })
                .orElseGet(() -> anonymousSessionRepository.save(new AnonymousSession(sessionId, now)));
    }
}

