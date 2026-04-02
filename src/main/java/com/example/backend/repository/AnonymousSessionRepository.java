package com.example.backend.repository;

import com.example.backend.domain.session.AnonymousSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// X-Session-Id 기반 익명 세션을 조회/생성하고, resetVersion/lastSeenAt 등 진행 상태를 저장할 때 사용
public interface AnonymousSessionRepository extends JpaRepository<AnonymousSession, UUID> {
}

