package com.example.backend.domain.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "anonymous_sessions")
public class AnonymousSession {

    @Id
    @Column(nullable = false, columnDefinition = "uuid")
    @Comment("익명 세션 ID(X-Session-Id)")
    private UUID sessionId;

    @Column(nullable = false)
    @Comment("세션 최초 생성 시각")
    private Instant createdAt;

    @Column(nullable = false)
    @Comment("세션 마지막 접근 시각")
    private Instant lastSeenAt;

    @Column(nullable = false)
    @Comment("진행 리셋 회차(현재 유효 회차)")
    private int resetVersion;

    protected AnonymousSession() {
    }

    public AnonymousSession(UUID sessionId, Instant now) {
        this.sessionId = sessionId;
        this.createdAt = now;
        this.lastSeenAt = now;
        this.resetVersion = 0;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public int getResetVersion() {
        return resetVersion;
    }

    public void touch(Instant now) {
        this.lastSeenAt = now;
    }

    public void resetProgress(Instant now) {
        this.resetVersion += 1;
        this.lastSeenAt = now;
    }
}

